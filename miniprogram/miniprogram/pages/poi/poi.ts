Page({
    data: {
        showSearchResult: false,
        searchResult: [],
        groupedData: [],
        types: ['a人文·历史·建筑·大学', 'b艺术·展馆·书苑·剧院', 'c行业·科学·技术·专业', 'd寺庙·坛观·宗教·教堂','e游乐·度假·表演·小镇', 'g商场·商圈·零售·百货', 'h园区·大厦·酒店·建筑', 'i公园·山水·自然·美景', 'k胡同·交通·美食·其他'],
        da: [],
        receivedParam: "",
        text: '',
        zhedie:false,
        currentGroupIndex: 0,
        showVideoPopup: false,
        popupVideoSrc: '',
        tabClickTime: 0,
        noNet:false,
        video:false,
      },
      onRefresherRefresh() {
        setTimeout(() => {
          this.selectComponent('#scroll').stopRefresher();
        }, 500);
      },
    onShow(){this.setData({noNet:getApp().globalData.noNet,video:getApp().globalData.video})},
    onLoad() {
      this.setData({noNet:getApp().globalData.noNet})
        wx.showLoading({title: '加载数据中...'});
        this.setData({
            da: getApp().globalData.myGlobalData
        })
        this.groupData();
    },
    groupData() {
        const originalData = this.data.da;
        const types = this.data.types;
        const groupNameCache = {};
        const grouped = {};
        originalData.forEach(item => {
          const groupName = item.type[0];
          if (!groupNameCache[groupName]) {
              groupNameCache[groupName] = this.getFullGroupName(groupName, types);
          }
          if (!grouped[groupName]) {
              grouped[groupName] = {
                  groupName: groupNameCache[groupName],
                  expanded: true,
                  items: []
              };
          }
          grouped[groupName].items.push(item);
      });
        console.warn("grouped")
        console.warn(grouped)

        // 对每个分组的 items 数组按评分从高到低排序
        for (const group in grouped) {
            grouped[group].items.sort((a, b) => {
                return b.rate - a.rate
                // return b.recomend.length - a.recomend.length
            });
            const itemCount = grouped[group].items.length;
            grouped[group].groupName = `${grouped[group].groupName}  (${itemCount})`;
        }
        const groupedData = Object.values(grouped);
        this.loadNextGroup(groupedData);
        // this.setData({
        //     groupedData
        // }, () => {
        //     wx.hideLoading();
        // });

    },
    loadNextGroup(groupedData) {
      const { currentGroupIndex } = this.data;
      wx.hideLoading();
      if (currentGroupIndex >= groupedData.length) {

          return;
      }
      const newGroup = groupedData[currentGroupIndex];
      this.setData({
          da: this.data.da.concat(newGroup.items),
          groupedData: this.data.groupedData.concat([newGroup]),
          currentGroupIndex: currentGroupIndex + 1
      }, () => {
          setTimeout(() => {
              this.loadNextGroup(groupedData);
          }, 0); // 可调整加载间隔时间
      });
  },
    getFullGroupName(groupName, types) {
        for (let i = 0; i < types.length; i++) {
            if (types[i][0] === groupName) {
                return types[i].slice(1);
            }
        }
        return groupName;
    },
    onSearchInput(e) {
        const keyword = e.detail.value.trim();
        if (keyword) {
            const result = this.data.da.filter(item => item.title.includes(keyword));
            this.setData({
                showSearchResult: true,
                searchResult: result,
            });
        } else {
            this.setData({
                showSearchResult: false,
                searchResult: []
            });
        }
    },
    onResultItemTap(e) {
        const index = e.currentTarget.dataset.index;
        const selectedMarker = this.data.searchResult[index];

        this.setData({
            showSearchResult: false,
            searchResult: []
        });
        // this.jumpToListItem(selectedMarker)
    },
    // jumpToMarker(marker) {
    //   const { mapCtx } = this.data;
    //   this.setData({
    //     scale: 14
    //   });
    //     mapCtx.moveToLocation({
    //       longitude: marker.longitude,
    //       latitude: marker.latitude,
    //     })

    // },
    jumpToListItem: function () {
        const targetId = 'place_0_0'; // 示例目标ID（根据实际需求修改）
        const [, groupIndexStr, placeIndexStr] = targetId.split('_');
        const groupIndex = parseInt(groupIndexStr);
        const placeIndex = parseInt(placeIndexStr);

        // 检查分组是否存在
        if (groupIndex < 0 || groupIndex >= this.data.groupedData.length) {
            console.error('目标分组不存在');
            return;
        }

        const currentGroup = this.data.groupedData[groupIndex];
        if (!currentGroup.expanded) {
            // 创建临时对象
            const dataToSet = {};
            const propertyName = `groupedData[${groupIndex}].expanded`;
            dataToSet[propertyName] = true;

            // 展开分组
            this.setData(dataToSet, () => {
                // 分组展开后执行滚动
                this.scrollToTarget(targetId);
            });
        } else {
            // 直接滚动
            this.scrollToTarget(targetId);
        }
    },

    scrollToTarget(targetId) {
        const query = wx.createSelectorQuery();
        query.select('#' + targetId).boundingClientRect();
        query.exec((res) => {
            if (res && res[0]) {
                const scrollTop = res[0].top;
                wx.pageScrollTo({
                    scrollTop: scrollTop,
                    duration: 300
                });
            }
        });
    },
    toggleGroup(e) {
        const index = e.currentTarget.dataset.index;
        const groupedData = this.data.groupedData;
        groupedData[index].expanded = !groupedData[index].expanded;
        this.setData({
            groupedData
        });
    },
//     closeVideoPopup() {
//       this.setData({
//           showVideoPopup: false
//       });
//   },
//   playVideo(e) {
//     const place = e.currentTarget.dataset.place;
//     if (place) {
//         this.setData({
//             showVideoPopup: true,
//             popupVideoSrc: place.video
//         });
//     }
// },
    onPlaceTap(e) {
        // const id = e.currentTarget.dataset.id;
        // console.log(`点击了景点 ID: ${id}`);
        const place = e.currentTarget.dataset.place;
        // const placeJson = JSON.stringify(place);
        const app = getApp();
        app.globalData.selectedPlace = place;
        app.globalData.source = 'video';
        console.warn("placeJson")
        wx.switchTab({
            url: `/pages/map/map`,
            success: function (res) {
                console.log('跳转成功');
            },
            fail: function (err) {
                console.error('跳转失败:', err);
            }
        });
    },
    backToTop: function () {
        wx.pageScrollTo({
            scrollTop: 0,
            duration: 300 // 滚动动画持续时间（毫秒）
        });
    },

    toggleEx: function () {
        const groupedData = this.data.groupedData;
        for (let i = 0; i < groupedData.length; i++) {
            groupedData[i].expanded = false
        }
        this.setData({
            groupedData,
            // zhedie:!this.data.zhedie
        });
    },

    onPlaceTap2(e) {
        const places = e.currentTarget.dataset.p;
        const title = e.currentTarget.dataset.t;
        // const placeJson = JSON.stringify(place);
        console.warn(places)
        console.warn(title)
        // const idsAsString = places.items.map(item => item.title).join(',')
        // console.warn(idsAsString)
        const numberArray = [];
        for (let i = 0; i < places.items.length; i++) {
            // numberArray.push(Number(places[i].id))
            numberArray.push(places.items[i].title)
        }

        getApp().globalData.source = 'video';
        getApp().globalData.filterItem = title;
        getApp().globalData.navIds = numberArray;
        wx.switchTab({
            url: `/pages/map/map`,
            success: function (res) {
                console.log('跳转成功');
            },
            fail: function (err) {
                console.error('跳转失败:', err);
            }
        });
        e.stopPropagation();
    },
    // onLoad(options: any) {
    //   const param = options.param;
    //   this.setData({
    //     receivedParam: param
    //   });
    // },
    c1: function () {
        wx.setClipboardData({

            data: this.data.text,
            // data: "asdfasdf",
            success(res) {
                // 复制成功后的操作，如弹出提示框告知用户
                wx.showToast({
                    title: '复制成功',
                })
            },
            fail(res) {
                // 复制失败后的操作，如打印错误信息
                console.log('复制失败：', res)
            },
            complete() {
                // 无论成功或失败都会执行的操作
            }
        })
    },
    readDataFromFile: function () {
        const fs = wx.getFileSystemManager();
        const filePath = `${wx.env.USER_DATA_PATH}/data.json`;

        fs.readFile({
            filePath: filePath,
            encoding: 'utf8',
            success: res => {
                console.log('文件读取成功', res.data);

                // const data = JSON.parse(res.data); // 将读取到的字符串解析为JSON对象
                this.setData({text: res.data});

                wx.showToast({
                    title: '读取成功',
                    icon: 'success'
                });
            },
            fail: err => {
                console.error('文件读取失败', err);
                wx.showToast({
                    title: '读取失败',
                    icon: 'none'
                });
            }
        });
    },
    // 监听tab的点击事件
  onTabItemTap(event) {
    const currentTime = new Date().getTime();
    // 判断是否是双击
    if (currentTime - this.data.tabClickTime < 300) {
      // 执行回到顶部的方法
    //   wx.pageScrollTo({
    //     scrollTop: 0,
    //     duration: 300
    //   });
    this.setData({
        scrollTop: 0 // 设置scrollTop为0，实现滚动到顶部的效果
      });
    }
    // 更新点击时间
    this.setData({
      tabClickTime: currentTime
    });
  }
});
