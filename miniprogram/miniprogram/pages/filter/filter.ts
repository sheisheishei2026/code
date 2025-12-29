Page({
  data: {
    placesList: [],
    filterPlacesList: [],
    placesList2: [],
    isFilterOpen: false,
    filterOptions: ['0全部', '1免费', '2收费', '3换票', '4一卡通','5中午休息', '6周日闭馆','8需要预约','760岁免费','9工作日开'],
    newFilterOptions: ['有视频', '有公众号', '有小程序','三星以上','公园年卡', '小吃饭店', '闲时可去', '时光记忆', '地铁直达','我的收藏'],
    newFilterOptions2: [ "骑行", "巨幕", "教堂","寺庙","民俗","长城", "园林","温泉" ,"夏日", "银杏"],
    newFilterOptions3: ['零星','一星','二星','三星','四五星',"六环外","美术馆","看演出",'看夜景', '去拍照'],
    nianka: ["颐和园", "天坛公园", "北海公园", "中山公园", "香山公园", "景山公园", "国家植物园", "动物园", "陶然亭公园", "玉渊潭公园", "百望山森林公园", "双秀公园", "北京国际雕塑公园", "大观园", "地坛公园", "水立方嬉水乐园"],
    selectedName: '0开启筛选',
    sortOption: '排序选项',
    sortedByRating: false,
    sortedByPrice: false,
    sortedByLocation: false,
    inputValue: '',
    pageSize: 30,
    currentPage: 1,
    hasMore: true,
    disableMultiSlide: false,
    lastScrollTop: 0,
    directionHistory: [],
    consecutiveThreshold: 3, // 连续相同方向滚动的次数阈值
    isShow: true,
    tabClickTime: 0,
    startY: 0,
    moveY: 0,
    touchStatus: false,
    threshold: 50, // 滑动阈值，超过这个距离才判定为滑动  "书苑", "胡同", "故居",
    noNet:false,
  },
  onShow(){this.setData({noNet:getApp().globalData.noNet})},
  loadData() {
    const { pageSize, currentPage, filterPlacesList } = this.data;
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = startIndex + pageSize;
    const newData = filterPlacesList.slice(startIndex, endIndex);
    if (newData.length < pageSize) {
      // 如果本次获取的数据量小于每页大小，说明没有更多数据了
      this.setData({
        hasMore: false
      });
    }

    this.setData({
      placesList: this.data.placesList.concat(newData),
      currentPage: currentPage + 1
    });
    console.warn('hasMore:' + this.data.hasMore)
    console.warn('currentPage:' + currentPage)
    console.warn('placesList:')
    console.warn(this.data.placesList)
  },
  onReachBottom() {
    console.warn('onReachBottom')
    const { hasMore } = this.data;
    if (hasMore) {
      this.loadData();
    }
  },
  toggleFilter() {
    this.setData({
      isFilterOpen: !this.data.isFilterOpen,
      isSortPopupOpen: false,
    });
  },
  selectFilterOption(e) {
    const selectedOption = e.currentTarget.dataset.option;
    this.setData({
      selectedName: selectedOption,
      isFilterOpen: false,
      // sortOption: '排序选项',
    });
    this.filterItem()
  },
  // selectNewFilterOption(e) {
  //     const selectedOption = e.currentTarget.dataset.option;
  //     this.setData({
  //         selectedName: selectedOption,
  //         isFilterOpen: false,
  //         // sortOption: '排序选项',
  //     });
  //     this.filterItem()
  // },
  selectNewFilterOption2(e) {
    const selectedOption = e.currentTarget.dataset.option;
    this.setData({
      selectedName: 'Z' + selectedOption,
      isFilterOpen: false,
      // sortOption: '排序选项',
    });
    this.filterItem()
  },
  onLoad2(reverse) {
    wx.showLoading({
      'title': "加载中" 
    })
    // if (reverse) {
    //   this.setData({
    //     placesList2: getApp().globalData.myGlobalData.reverse()
    //   })
    // }
    // else {
    //   this.setData({
    //     placesList2: getApp().globalData.myGlobalData
    //   })
    // }
    this.setData({
      // placesList2: getApp().globalData.myGlobalData.reverse()
      placesList2: reverse ? getApp().globalData.myGlobalData.reverse() : getApp().globalData.myGlobalData
    })
    wx.getSetting({
      success(res) {
        if (!res.authSetting['scope.userLocation']) {
          wx.openSetting({
            success: (res) => {
            }
          });
        } else {
        }
      }
    });
    const that = this;
    const lat = getApp().globalData.latitude
    const lon = getApp().globalData.longitude
    // 如果有定位，更新原始数据
    if (lat > 0 && lon > 0) {
      const data = that.init(lat, lon)
      wx.hideLoading()
      that.setData({
        placesList2: data,
        filterPlacesList: data,
      })
    } else {
      wx.hideLoading()
      that.setData({
        filterPlacesList: that.data.placesList2,
      })
      // 加载无定位的列表数据,不请求定位了
      // wx.getLocation({
      //     type: 'gcj02',
      //     success(res) {
      //         const lat = res.latitude;
      //         const lon = res.longitude;
      //         getApp().latitude = lat
      //         getApp().longitude = lon
      //         that.init(lat, lon)
      //     },
      //     fail(err) {
      //         console.error('获取位置失败', err);
      //         // that.setData({ placesList: that.data.placesList2 }, () => {
      //         //     wx.hideLoading()
      //         // });
      //         wx.hideLoading()
      //         that.loadData()
      //     }
      // });
    }
    that.loadData()
  },
  onPullDownRefresh() {
    this.onLoad()
    this.reload()
    wx.stopPullDownRefresh(); 
  },
  onLoad() {
    this.getVer()
    this.setData({
        noNet:getApp().globalData.noNet,
        selectedName: '0开启筛选',
        sortOption: '排序选项',
        sortedByRating: false,
        sortedByPrice: false,
        sortedByLocation: false,
        inputValue: '',
    })
    this.onLoad2(false)
  },
  init(lat, lon) {
    const that = this
    let placesListWithDistance
    if (lat && lon) {
      const { placesList2 } = that.data;
      const earthRadius = 6371;
      placesListWithDistance = placesList2.map(place => {
        const dLat = (place.latitude - lat) * (Math.PI / 180);
        const dLon = (place.longitude - lon) * (Math.PI / 180);
        const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos(lat * (Math.PI / 180)) * Math.cos(place.latitude * (Math.PI / 180)) *
          Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        let distance = earthRadius * c;
        distance = parseFloat(distance.toFixed(1));

        return {
          ...place,
          distance: distance
        };
      });
    }
    return placesListWithDistance
  },
  need(place) {
    return place.time.includes('预约')
  },
  filterItem() {
    this.clearInput()
    const { selectedName, placesList2 } = this.data;
    const filtered = placesList2.filter(item => {
      const typeMatch = Number(selectedName[0]) == 0 ||
          (selectedName[0] == '9' && item.time.includes('仅工作日')) ||
          (selectedName[0] == '8' && item.time.includes('预约')) ||
          (selectedName[0] == '7' && item.price.includes('60岁免费')) ||
          (selectedName[0] == '6' && item.time.includes('周日闭馆')) ||
          (selectedName[0] == '5' && item.time.includes('中午休息')) ||
          (selectedName[0] == '4' && (item.price.includes('一卡通') || item.recomend.includes('一卡通'))) ||
          (selectedName[0] == '3' && item.price.includes('换票')) ||
          (selectedName[0] == '2' && (this.getPrice(item) > 0)) ||
          (selectedName[0] == '1' && (this.getPrice(item) == 0)) ||
          (selectedName.includes('有视频') && item.video && item.video != '') ||
          (selectedName.includes('时光记忆') && item.type.includes('p')) ||
          (selectedName.includes('小吃饭店') && item.type.includes('v')) ||
          (selectedName.includes('夜景') && item.recomend.includes('夜景')) ||
          (selectedName.includes('四合院') && item.type.includes('q')) ||
          (selectedName.includes('巨幕') && item.recomend.includes('幕')) ||
          (selectedName.includes('民俗') && item.type.includes('o')) ||
          (selectedName.includes('长城') && item.title.includes('长城')) ||
          (selectedName.includes('园林') && item.recomend.includes('园林')) ||
          (selectedName.includes('看演出') && (item.recomend.includes('表演') || item.recomend.includes('演出'))) ||
          (selectedName.includes('去拍照') && item.recomend.includes('适合拍照')) ||
          (selectedName.includes('骑行') && item.recomend.includes('骑行')) ||
          (selectedName.includes('美术馆') && (item.title.includes('美术') || item.recomend.includes('美术'))) ||
          (selectedName.includes('胡同') && (item.title.includes('胡同') || item.recomend.includes('胡同'))) ||
          (selectedName.includes('故居') && (item.title.includes('故居') || item.recomend.includes('故居') || item.title.includes('纪念馆') || item.recomend.includes('纪念馆'))) ||
          (selectedName.includes('教堂') && (item.title.includes('教堂') || item.recomend.includes('教堂'))) ||
          (selectedName.includes('寺庙') && (item.title.includes('寺') || item.recomend.includes('寺庙'))) ||
          (selectedName.includes('温泉') && (item.recomend.includes('温泉') || item.recomend.includes('汤泉'))) ||
          (selectedName.includes('六环外') && (Number(item.longitude) > 116.706554 || Number(item.longitude) < 116.139816 || Number(item.latitude) > 40.158419 || Number(item.latitude) < 39.72192)) ||
          (selectedName.includes('运动健身') && (item.recomend.includes('健身') || (item.recomend.includes('锻炼') || item.recomend.includes('体育'))) ||
              (selectedName.includes('公园年卡') && this.data.nianka.includes(item.title)) ||
              (selectedName.includes('夏日') && (item.recomend.includes('肌肉') || item.recomend.includes('游泳'))) ||
              (selectedName.includes('银杏') && (item.recomend.includes('秋天') || item.recomend.includes('银杏'))) ||
              (selectedName.includes('地铁直达') && (item.recomend.includes('地铁直达'))) ||
              (selectedName.includes('闲时可去') && (item.type.includes('x'))) ||
              (selectedName.includes('有公众号') && item.officalId != "") ||
              (selectedName.includes('有小程序') && item.appId != "") ||
              (selectedName.includes('我的收藏') && (wx.getStorageSync('favorites') || []).includes(item.title)) ||
              (selectedName.includes('三星以上') && Number(item.rate) > 2) ||
              (selectedName.includes('零星') && Number(item.rate) == 0) ||
              (selectedName.includes('一星') && Number(item.rate) == 1) ||
              (selectedName.includes('二星') && Number(item.rate) == 2) ||
              (selectedName.includes('三星') && Number(item.rate) == 3) ||
              (selectedName.includes('四五星') && (Number(item.rate) == 4||Number(item.rate) == 5)) ||
              item.type.includes(selectedName[0]))
      return typeMatch;
    });
    if (this.data.sortOption == '按评分排序') {
      filtered.sort((a, b) => {
        return !this.data.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
      });
    } else if (this.data.sortOption == '按价格排序') {
      filtered.sort((a, b) => {
        return !this.data.sortedByPrice ? this.getPrice(a) - this.getPrice(b) : this.getPrice(b) - this.getPrice(a);
      });
    } else if (this.data.sortOption == '按距离排序') {
      filtered.sort((a, b) => a.distance - b.distance);
    }
    this.setData({ filterPlacesList: filtered }, () => {
      this.reload()
    });
  },
  resetHeader() {
    this.setData({
      placesList: [],
      pageSize: 20,
      currentPage: 1,
      hasMore: true
    })
  },
  close() {
    this.setData({
      isFilterOpen: false,
      isSortPopupOpen: false,
    });
  },
  goToMap() {
    const places = this.data.filterPlacesList
    let title
    if (this.data.inputValue != '') {
      title = this.data.inputValue
    } else if (this.data.selectedName == '0开启筛选') {
      title = '全部'
    } else {
      title = this.data.selectedName.slice(1)
    }
    const numberArray = [];
    for (let i = 0; i < places.length; i++) {
      // numberArray.push(Number(places[i].id))
      numberArray.push(places[i].title)
    }
    // console.warn(numberArray)

    getApp().globalData.filterItem = title
    getApp().globalData.navIds = numberArray;
    getApp().globalData.source = 'filter';
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
  toggleSortPopup() {
    this.setData({
      isSortPopupOpen: !this.data.isSortPopupOpen,
      isFilterOpen: false,
    });
  },
  closeSortPopup() {
    this.setData({
      isSortPopupOpen: false
    });
  },
  getPrice(item) {
    //   if (item.pricenum && item.pricenum!= '') {
    //     return Number(item.pricenum)
    // } else {
    //     return Number(item.price)
    // }
    if (item.price == '' || item.price == '0') {
      return 0
    } else {
      const firstPart = item.price.split('，')[0].replace('元', '')
      return Number(firstPart)
      // return '￥' + firstPart + '元'
    }
  },
  sortByTime() {
    this.setData({
      sortOption: '按添加排序',
      sortedByRating: !this.data.sortedByRating,
      sortedByPrice: false,
      sortedByLocation: false,
      selectedName: '0开启筛选',
    })
    this.onLoad2(true)
    this.reload()
  },
  sortByRating(e) {
    this.setData({
      sortOption: '按评分排序',
      // inputValue: '',
    })
    let placesList = this.data.filterPlacesList;
    placesList.sort((a, b) => {
      return this.data.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
    });
    this.setData({
      sortedByRating: !this.data.sortedByRating,
      sortedByPrice: false,
      sortedByLocation: false,
    });
    this.reload()
  },
  sortByPrice() {
    this.setData({
      sortOption: '按价格排序',
      // inputValue: '',
    })
    let placesList = this.data.filterPlacesList
    placesList.sort((a, b) => {
      return this.data.sortedByPrice ? this.getPrice(a) - this.getPrice(b) : this.getPrice(b) - this.getPrice(a);
      // return this.data.sortedByPrice ? a.recomend.length - b.recomend.length : b.recomend.length - a.recomend.length
    });
    this.setData({
      sortedByPrice: !this.data.sortedByPrice,
      sortedByRating: false,
      sortedByLocation: false,
    });
    this.reload()
  },
  addDistanceToFilterList() {
    const { placesList2, filterPlacesList } = this.data;
    const newFilterPlacesList = filterPlacesList.map(item => {
      const match = placesList2.find(place => place.title == item.title);
      if (match) {
        return { ...item, distance: match.distance };
      }
      return item;
    });
    newFilterPlacesList.sort((a, b) => a.distance - b.distance);
    this.setData({
      filterPlacesList: newFilterPlacesList
    });
  },
  sort(latitude, longitude) {
    const that = this
    const data = that.init(latitude, longitude)
    // 按距离排序
    data.sort((a, b) => a.distance - b.distance);
    this.setData({
      sortOption: '按距离排序',
      sortedByRating: false,
      sortedByPrice: false,
      sortedByLocation: true,
      // inputValue: '',
    })
    that.setData({
      placesList2: data,
    }, () => {
      that.resetHeader()
      that.addDistanceToFilterList()
      that.loadData()
      that.backToTop();
    })
  },
  sortByDistance() {
    const that = this;
    if (getApp().globalData.latitude > 0 && getApp().globalData.longitude > 0) {
      that.sort(getApp().globalData.latitude, getApp().globalData.longitude)
    } else {
      wx.getLocation({
        type: 'gcj02',
        success(res) {
          const latitude = res.latitude;
          const longitude = res.longitude;
          getApp().globalData.latitude = latitude
          getApp().globalData.longitude = longitude
          if (latitude && longitude) {
            console.warn('定位成功')
            that.sort(latitude, longitude)
          }
        },
        fail(err) {
          console.error('获取位置失败', err);
          if (getApp().globalData.latitude > 0 && getApp().globalData.longitude > 0) {
            // that.sortByDistance2(getApp().latitude, getApp().longitude)
            that.sort(getApp().globalData.latitude, getApp().globalData.longitude)
          } else {
            wx.showToast({
              title: '定位失败，请开启定位权限',
              icon: 'error'
            })
            wx.getSetting({
              success(res) {
                if (!res.authSetting['scope.userLocation']) {
                  wx.openSetting({
                    success: (res) => {
                    }
                  });
                } else {
                }
              }
            });
          }
        }
      });
    }
  },
  // sortByDistance2(lat, lon) {
  //     const { placesList } = this.data;
  //     const earthRadius = 6371; // 地球半径，单位：千米

  //     // 计算每个地点与当前位置的距离
  //     const placesListWithDistance = placesList.map(place => {
  //         const dLat = (place.latitude - lat) * (Math.PI / 180);
  //         const dLon = (place.longitude - lon) * (Math.PI / 180);
  //         const a =
  //             Math.sin(dLat / 2) * Math.sin(dLat / 2) +
  //             Math.cos(lat * (Math.PI / 180)) * Math.cos(place.latitude * (Math.PI / 180)) *
  //             Math.sin(dLon / 2) * Math.sin(dLon / 2);
  //         const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  //         let distance = earthRadius * c;
  //         distance = parseFloat(distance.toFixed(1));

  //         return {
  //             ...place,
  //             distance: distance
  //         };
  //     });

  //     // 按距离排序
  //     placesListWithDistance.sort((a, b) => a.distance - b.distance);

  //     this.setData({
  //         placesList: placesListWithDistance
  //     });
  //     this.backToTop();
  //     // this.closeSortPopup();
  // },
  onListScroll(e) {
    this.setData({
      isSortPopupOpen: false,
      isFilterOpen: false,
    });
    console.warn('onListScroll')
    this.close()

    const scrollTop = e.detail.scrollTop;
    const lastScrollTop = this.data.lastScrollTop;
    console.log('列表滑动距离顶部的距离:', scrollTop);
    if (scrollTop - lastScrollTop > 3) {
      console.log('列表向下滑动');
      this.setData({
        isShow: false
      });

    } else if (scrollTop - lastScrollTop < -3) {
      console.log('列表向上滑动');
      this.setData({
        isShow: true
      });
    }
    this.setData({
      lastScrollTop: scrollTop
    });
  },

  onListItemClick(e) {
    this.close()
    const place = e.currentTarget.dataset.item;
    console.log('点击了列表项:', place);
    const app = getApp();
    app.globalData.selectedPlace = place;
    app.globalData.source = 'filter';
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
      duration: 300
    });
    this.setData({
      isShow: true
    });
    // this.setData({
    //     scrollTop: 0
    //   });
  },

  checkTimeDifference() {

    // const timeDiffMs = Date.now() - 1754882650603;
    // const timeDiffDays = timeDiffMs / (1000 * 60 * 60 * 24);
    // return timeDiffDays >= 5
  },

  getVer() {
    try {
      // const accountInfo = wx.getAccountInfoSync();
      // const envVersion = accountInfo.miniProgram.envVersion;
      // let st = ""
      // switch (envVersion) {
      //   case 'develop':
      //     console.log('当前是开发版');
      //     st = "develop"
      //     break;
      //   case 'trial':
      //     console.log('当前是体验版');
      //     st = "trial"
      //     break;
      //   case 'release':
      //     console.log('当前是正式版');
      //     st = "release"
      //     break;
      //   default:
      //     console.log('未知环境');
      // }
      console.warn(Date.now())
      const timeDiffMs = Date.now() - 1763954568143;
      const timeDiffDays = timeDiffMs / (1000 * 60 * 60 * 24);
      console.warn(timeDiffDays)
      if(timeDiffDays>5){
        getApp().globalData.video = true
        getApp().globalData.pub = false
      }else{
        getApp().globalData.video = false
        getApp().globalData.pub = true
      }
      // if (getApp().globalData.pub) {
      //   getApp().globalData.video = false
      //   // const request4 = this.fetchData('https://api.jsonbin.io/v3/b/67c83233ad19ca34f816e4eb');
      //   // Promise.all([request4])
      //   // .then(([response4]) => {
      //   //   // console.log(response4)
      //   //   // console.log(response4.data.record)
      //   //   getApp().globalData.video = response4.data.record.video
      //   // })
      // }else{
      //   getApp().globalData.video = true
      // }
    } catch (e) { console.error(e) }
  },
  fetchData(url) {
    return new Promise((resolve, reject) => {
      wx.request({
        url,
        method: 'GET',
        header: {
          'X-Master-Key': '$2a$10$G6PisFAdShe42JfDEysF.ulRQoXeYX.l19Xlhj552pgH4DOlw.cby',
          'Content-Type': 'application/json'
        },
        success: (res) => {
          console.warn(res)
          resolve(res);
        },
        fail: (err) => {
          reject(err);
          console.warn(err)
          wx.showToast({
            title: '请求失败',
            icon: 'none'
          });
        }
      });
    });
  },
  handleFilterTouchMove(e) {
    // 阻止事件冒泡到列表，确保筛选浮层滑动时列表不滑动
    e.stopPropagation();
  },
  handleListTouchMove(e) {
    if (this.data.isFilterOpen) {
      // 如果筛选浮层展开，阻止列表滑动
      e.preventDefault();
      return;
    }
    // 如果筛选浮层关闭，允许列表正常滑动
  },
  onSearchInput(e) {
    this.setData({
      inputValue: e.detail.value,
      selectedName: '0开启筛选',
      // sortedByPrice: false,
      // sortedByRating: false,
      // sortOption: '排序选项',
      hasMore: false,
    });
    const keyword = e.detail.value.trim();
    if (keyword != '') {
      const result = this.data.placesList2.filter(item =>
        item.title.includes(keyword) || item.recomend.includes(keyword)
        || item.time.includes(keyword) || item.price.includes(keyword)
      );
      if (this.data.sortOption == '按评分排序') {
        result.sort((a, b) => {
          return !this.data.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
        });
      } else if (this.data.sortOption == '按价格排序') {
        result.sort((a, b) => {
          return !this.data.sortedByPrice ? this.getPrice(a) - this.getPrice(b) : this.getPrice(b) - this.getPrice(a);
        });
      } else if (this.data.sortOption == '按距离排序') {
        result.sort((a, b) => a.distance - b.distance);
      }
      this.setData({
        filterPlacesList: result,
        placesList: result,
      }, () => {
        this.backToTop()
      });
    } else {
      this.setData({ filterPlacesList: this.data.placesList2 })
      this.reload()
    }

  },
  clearInput() {
    this.setData({
      inputValue: ''
    });
  },
  reload() {
    this.resetHeader()
    this.loadData()
    this.backToTop()
  },
  // 监听tab的点击事件
  onTabItemTap(event) {
    const currentTime = new Date().getTime();
    // 判断是否是双击
    if (currentTime - this.data.tabClickTime < 300) {
      // 执行回到顶部的方法
      wx.pageScrollTo({
        scrollTop: 0,
        duration: 300
      });
      this.setData({
        isShow: true
      });
      //   this.setData({
      //     scrollTop: 0
      //   });
    }
    this.setData({
      tabClickTime: currentTime
    });
  },
  handleTouchStart: function (e) {
    const touch = e.changedTouches[0];
    this.setData({
      startY: touch.clientY,
      touchStatus: true
    });
  },

  handleTouchMove: function (e) {
    if (!this.data.touchStatus) {
      return;
    }
    const touch = e.changedTouches[0];
    this.setData({
      moveY: touch.clientY
    });

    const { startY, moveY, threshold } = this.data;
    const distance = startY - moveY;
    if (Math.abs(distance) > threshold) {
      if (distance > 3) {
        console.log('向下滑动');
        this.setData({
          isShow: false,
          isFilterOpen: false,
          isSortPopupOpen: false
        });
      } else if (distance < -3) {
        console.log('向上滑动');
        this.setData({
          isShow: true,
          isFilterOpen: false,
          isSortPopupOpen: false
        });
      }
    }
  },

  handleTouchEnd: function (e) {
    this.setData({
      touchStatus: false
    });
  }

  // handleTouchMove: function (e) {
  //   if (!this.data.touchStatus) {
  //     return;
  //   }
  //   const touch = e.changedTouches[0];
  //   this.setData({
  //     moveY: touch.clientY
  //   });
  // },

  // handleTouchEnd: function (e) {
  //   this.setData({
  //     touchStatus: false
  //   });
  //   const { startY, moveY, threshold } = this.data;
  //   const distance = startY - moveY;
  //   if (Math.abs(distance) > threshold) {
  //     if (distance > 10) {
  //       console.log('向下滑动');
  //        this.setData({
  //           isShow: false,
  //           isFilterOpen:false,
  //           isSortPopupOpen:false,
  //         });
  //     } else  if (distance < -10) {
  //       console.log('向上滑动');
  //       this.setData({
  //           isShow: true,
  //           isFilterOpen:false,
  //           isSortPopupOpen:false,
  //         });
  //     }
  //   }
  // }
})
