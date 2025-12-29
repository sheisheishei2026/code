Page({
  data: {
    showTop: true,
    load: false,
    first: true,
    isDragging: false,
    startX: 0,
    startY: 0,
    setting: ['点击锚定', '清除收藏', '清除图标', '自动播放','导出收藏', '切换模式', '圆形图标','无网模式'],
    types: ['0全部', '1换票', '2一卡通', '7两星以上', '3三星以上','6屏蔽零星', '4小吃饭店', '5我的收藏'],
    showPoi: true,
    isJuhe: false,
    clickScale: true,
    rateColor: true,
    noNet: false,
    selectedName: '0全部',
    selectedSetting: '',
    showTypeDropdown: false,
    showSettingDropdown: false,
    longitude2: 116.397029,
    latitude2: 39.917839,
    hasLoaded: false,
    showSearchResult: false,
    searchResult: [],
    mapCtx: null,
    scale: 14,
    overlayImage: {},
    nearbyPois: [],
    address: '',
    tags: [],
    items: [],
    binData: null,
    markerCluster: {
      enable: true,
      styles: [{
        url: '/images/m2.png',
        width: 40,
        height: 40,
        textColor: '#ffffff',
        textSize: 14
      }]
    },
    batchSize: 0, // 每批加载的数量
    currentBatch: 1, // 当前批次
    totalBatches: 10, // 总批次数
    viewport: { // 地图视野范围
      northeast: {
        latitude: 0,
        longitude: 0
      },
      southwest: {
        latitude: 0,
        longitude: 0
      }
    },
    items2: [],
    nianka: ["颐和园", "天坛公园", "北海公园", "中山公园", "香山公园", "景山公园", "国家植物园北园", "动物园", "陶然亭公园", "玉渊潭公园", "百望山森林公园", "双秀公园", "北京国际雕塑公园", "大观园", "地坛公园", "水立方"],
    imgUrls: [
    ],
    current: 0,
    indicatorDots: false,
    autoplay: false,
    interval: 3000,
    duration: 300,
    circular: true,
    hasVideo: false,
    hasVideoReal: false,
    showVideoPopup: false,
    popupVideoSrc: '',
    autoVideo: false,
    currentTime: 0,
    enableVideo: false,
    video: false,

    tempMarkerImages: {}, // 缓存生成的图片路径
    pendingMarkers: [],// 待处理的 marker 队列
    cachedData: [],
    unprocessedMarkers: [],
    isConnected: true,
    networkType: '',
  },
  networkListener:null,
  netInit() {
    this.checkNetworkStatus();
    // 2. 监听网络状态变化（核心接口）
    this.networkListener = wx.onNetworkStatusChange(res => {
      // res.isConnected: 布尔值，是否有网络连接
      // res.networkType: 字符串，网络类型（wifi/2g/3g/4g/5g/unknown/none）
      this.setData({
        isConnected: res.isConnected,
        networkType: res.networkType
      });
      // 3. 网络断开回调
      if (!res.isConnected) {
        wx.showToast({
          title: '网络已断开，请检查网络',
          icon: 'none',
          duration: 3000
        });
        this.onNetworkDisconnected(); // 自定义网络断开处理函数
      }

      // 4. 网络恢复回调
      if (res.isConnected && !this.data.isConnected) {
        wx.showToast({
          title: `网络已恢复（${res.networkType}）`,
          icon: 'none',
          duration: 2000
        });
        this.onNetworkRestored(); // 自定义网络恢复处理函数
      }
    });
  },

  checkNetworkStatus() {
    wx.getNetworkType({
      success: res => {
        // 区分有网络（wifi/4g等）和无网络（none）
        const isConnected = res.networkType !== 'none';
        this.setData({
          isConnected,
          networkType: res.networkType
        });
      }
    });
  },

  // 自定义网络断开处理（例如暂停数据请求、显示离线提示）
  onNetworkDisconnected() {
    console.log('网络已断开，执行离线逻辑...');
    this.setData({
      clickScale:false
    })
  },

  // 自定义网络恢复处理（例如重新请求数据、隐藏离线提示）
  onNetworkRestored() {
    console.log('网络已恢复，执行恢复逻辑...');
    this.setData({
      clickScale:true
    })
  },

  // 页面卸载时移除监听（避免内存泄漏）
  onUnload() {
    if (this.networkListener) {
      this.networkListener(); // 移除网络监听
    }
  },

  processMarkerQueue() {
    if (this.data.pendingMarkers.length === 0) {
      // 一次只处理 10 个（避免阻塞 UI）
      const pending = this.data.unprocessedMarkers.splice(0, 10);
      this.setData({
        pendingMarkers: pending,
        unprocessedMarkers: this.data.unprocessedMarkers // 更新剩余未处理项
      });
      // const pending = this.data.unprocessedMarkers.slice(0, 10);
      // const remaining = this.data.unprocessedMarkers.slice(10);
      // this.setData({
      //   pendingMarkers: pending,
      //   unprocessedMarkers: remaining
      // });
    }
    // 处理队列中的第一个 marker
    const { pendingMarkers } = this.data;
    const marker = pendingMarkers[0];
    if (marker) {
      this.createRoundedMarker(marker);
    }
  },

  onHide() {
    this.setData({
      showTypeDropdown: false,
      showSettingDropdown: false,
      autoplay: false
    });
    getApp().globalData.selectedPlace = null;
    getApp().globalData.filterItem = null;
    getApp().globalData.navIds = null;
    getApp().globalData.source = '';
  },

  processReq() {
    try {
        if (this.checkTimeDifference()){
            this.clearMarkerCache()
        }
        this.circleIcon()
      const app = getApp();
      const place = app.globalData.selectedPlace;
      const filterItem = app.globalData.filterItem;
      const navIds = app.globalData.navIds;
      const source = app.globalData.source;
      // console.warn("source:"+source)
      if (navIds && navIds.length > 0) {
        this.closeOverlay()
        if (!this.data.load) {
          this.setData({
            isJuhe: false
          })
          this.getLocalMarker(1);
          // } else if (this.data.isJuhe) {
          //   this.setData({
          //     isJuhe: false
          //   })
          //   this.getLocalMarker();
        }
        if (filterItem) {
          this.setData({
            selectedName: '-' + filterItem
          })
          this.log(filterItem + "\n共" + navIds.length + "个地点")
        }
        if (filterItem == '全部') {
          this.setData({
            scale: this.data.scale - 0.5,
            selectedName: '0全部'
          });
        } else {
          // console.warn(navIds)
          // console.warn(this.data.items2)
          const filteredArray = this.data.items2.filter(item => navIds.includes(item.title))
          console.warn(filteredArray)
          const { mapCtx } = this.data;
          const markers = this.data.items;
          // 逐个移除marker
          markers.forEach((marker) => {
            mapCtx.removeMarkers({
              markerIds: [marker.id]
            });
          });
          this.closeOverlay()
          this.setData({
            items: [],
            // selectedName: '-全部',
            showTypeDropdown: false,
            showSettingDropdown: false,
          }, () => {
            this.setData({
              items: filteredArray
            });

            const { mapCtx } = this.data;
            const points = filteredArray.map(marker => ({
              latitude: marker.latitude,
              longitude: marker.longitude
            }));
            mapCtx.includePoints({
              points: points,
              // 上、右、下、左
              padding: [100, 50, 30, 50],
            });
          });
        }
      } else if (place) {
        console.warn("source:" + source);
        console.warn(source);
        if (source == 'video' && getApp().globalData.video) {
          this.setData({
            autoVideo: true,
            enableVideo: true,
          });
        } else {
          this.setData({
            autoVideo: false,
            // enableVideo: false,
          });
        }
        if (!this.data.load) {
          // this.setData({
          //     isJuhe:false
          // })
          this.getLocalMarker(2);
        }
        this.setData({
          // selectedName: '-详细信息',
          selectedName: '0全部',
          longitude2: place.longitude,
          latitude2: place.latitude,
        });
        this.processTag(place)
        this.setData({
          items: []
        })
        this.closeOverlay()
        this.addMarkerNot(place)
        place.isFav = this.isFavorites(place)
        this.setData({
          overlayImage: place,
        });
        this.imgPlay(place)
        this.processDis(place)
      } else if (this.data.first) {
        if (!this.data.load) {
          this.setData({
            isJuhe: true
          })
          this.getLocalMarker(5);
        }
        this.setData({
          longitude2: 116.397029,
          latitude2: 39.917839,
        });
        const { mapCtx } = this.data;
        mapCtx.moveToLocation({
          longitude: 116.397029,
          latitude: 39.917839,
        });
        // setTimeout(() => {
        // 计算每批加载的数量和总批次数
        // const batchSize = Math.ceil(this.data.items2.length / this.data.totalBatches);
        // this.setData({
        //     batchSize,
        // totalBatches: 100
        // }, () => {
        // this.loadBatchMarkers();
        // });
        // 不需要
        //   this.setData({
        //     items: this.data.items2
        //   })
        // }, 800);
        setTimeout(() => { this.location() }, 900)
      }
      this.setData({ first: false });
    } catch (error) {
      console.error('数据解析失败:', error);
    }
  },
  isLocationInRange(targetLng, targetLat, centerLng, centerLat, radius) {
    const EARTH_RADIUS = 6371;
    const toRadians = (degrees) => degrees * Math.PI / 180;
    const dLng = toRadians(targetLng - centerLng);
    const dLat = toRadians(targetLat - centerLat);
    const lat1 = toRadians(centerLat);
    const lat2 = toRadians(targetLat);
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    const distance = EARTH_RADIUS * c;
    return distance <= radius;
  },
  processTag(place) {
    const y = this.getType(place.type)
    let tagArray = []
    // if (place.recomend == null || place.recomend == '') {
    //   tagArray = tagArray.concat(y)
    // } else
    {
      tagArray = place.recomend.split('，');
      // tagArray.unshift(y);
    }
    const tags2 = tagArray.map(text => {
      return { text, color: this.getRandomColor() }
    });
    this.setData({ tags: tags2 });
  },
  loadBatchMarkers(filtered) {
    console.warn('batchSize')

    const { items, batchSize, currentBatch, totalBatches } = this.data;
    const startIndex = (currentBatch - 1) * batchSize;
    const endIndex = startIndex + batchSize;
    const newMarkers = filtered.slice(startIndex, endIndex);
    // console.warn(batchSize)
    // console.warn(currentBatch)
    // console.warn(totalBatches)
    this.setData({
      items: items.concat(newMarkers)
    }, () => {
      console.warn(items)
      if (currentBatch < totalBatches) {
        // 如果还有下一批，继续加载
        this.setData({
          currentBatch: currentBatch + 1
        }, () => {
          this.loadBatchMarkers(filtered);
        });
      }
    });
  },
  checkMarkersInView(detail) {
    // console.warn('checkMarkersInView')
    const { northeast, southwest } = this.data.viewport;
    const { selectedName } = this.data
    // console.warn(selectedName)
    // console.warn(southwest)
    const { items, items2 } = this.data;
    const newMarkers: any[] = [];

    let res
    if (selectedName.includes('全部')) {
      res = items2
    } else if (selectedName.includes('三星以上')) {
      res = items2.filter(item => Number(item.rate) > 2)
    }
    res.forEach(marker => {
      const { latitude, longitude } = marker;
      // 精确判断 marker 是否在视野范围内
      if (
        latitude <= northeast.latitude &&
        latitude >= southwest.latitude &&
        longitude <= northeast.longitude &&
        longitude >= southwest.longitude &&
        items.findIndex(m => m.title == marker.title) == -1
      ) {
        newMarkers.push(marker);
      }
    });
    // setTimeout(() => {
    this.setData({
      items: items.concat(newMarkers)
      //   items: newMarkers
    });
    // }, 300);
  },
  getType(str) {
    switch (str[0]) {
      case 'a':
        return '人文·历史·大学'
      case 'b':
        return '艺术·展馆·书苑'
      case 'c':
        return '行业·科学·技术'
      case 'd':
        return '寺庙·坛观·宗教'
      case 'e':
        return '游乐·度假·表演'
      case 'g':
        return '商场·商圈·零售'
      case 'h':
        return '园区·大厦·酒店'
      case 'i':
        return '公园·山水·自然'
      case 'k':
        return '街道·美食·小吃'
    }
  },
  addMarkerNot(place) {
    console.warn(place)
    let items4 = this.data.items;
    if (this.data.items.some(item => item.title == place.title)) {
    } else {
      const index = this.data.items2.findIndex(item => item.title === place.title);
      if (index !== -1) {
        // console.log(`找到索引：${index}`);
        const newItem = this.getMarker(place, index, 1)
        items4.push(newItem);
        this.setData({ items: items4 })
      } else {
        console.log("未找到匹配的title");
      }


    }
    this.processTag(place)
    this.getRgc(place);
    if (this.data.clickScale) {
      this.goTo(place)
    }
    this.processDis(place)
  },
  saveTimer: null,
  saveTempPathToStorage(key, tempFilePath) {
    const { cachedData } = this.data;
    const newCachedData = {
      ...cachedData,
      [key]: { tempFilePath }
    };
    this.setData({ cachedData: newCachedData });
    wx.setStorageSync('markerCache', newCachedData);

    // // 2. 防抖处理：100ms 内多次更新只写一次 storage
    // if (this.saveTimer) {
    //   clearTimeout(this.saveTimer); // 清除已有定时器
    // }
    // // 重新设置定时器，100ms 后写入 storage
    // this.saveTimer = setTimeout(() => {
    //   wx.setStorageSync('markerCache', newCachedData);
    //   this.saveTimer = null; // 执行后清空定时器
    // }, 500);
  },

  getCachedPath(key) {
    const { cachedData } = this.data;
    if (!cachedData) return null;
    const cachedItem = cachedData[key];
    if (!cachedItem) return null;
    if (cachedItem.tempFilePath && cachedItem.tempFilePath.length > 0) {
      return cachedItem.tempFilePath
    } else {
      return null
    }
  },

  createRoundedMarker(place) {
    {
      console.warn("createRoundedMarker")
      const ctx = wx.createCanvasContext('markerCanvas');
      const diameter = 40; // 圆的直径
      const radius = diameter / 2; // 圆的半径

      ctx.beginPath();
      ctx.arc(radius, radius, radius, 0, Math.PI * 2);
      ctx.closePath();
      ctx.clip();

      const that = this
      // 加载图片并绘制
      wx.getImageInfo({
        src: place.img,
        success(res) {
          const imgWidth = res.width;
          const imgHeight = res.height;

          // 计算图片的宽高比
          const imgRatio = imgWidth / imgHeight;
          const canvasRatio = 1; // 画布是正方形（直径 x 直径）

          let drawWidth, drawHeight, drawX, drawY;

          // 根据宽高比决定如何缩放图片，保持居中且不变形
          if (imgRatio > canvasRatio) {
            // 图片比画布宽，以高度为基准缩放
            drawHeight = diameter;
            drawWidth = drawHeight * imgRatio;
            drawX = radius - drawWidth / 2;
            drawY = 0;
          } else {
            // 图片比画布高，以宽度为基准缩放
            drawWidth = diameter;
            drawHeight = drawWidth / imgRatio;
            drawX = 0;
            drawY = radius - drawHeight / 2;
          }
          // 绘制图片（居中且按比例缩放）
          ctx.drawImage(res.path, drawX, drawY, drawWidth, drawHeight);

          ctx.beginPath();
          ctx.arc(radius, radius, radius, 0, Math.PI * 2);
          ctx.setStrokeStyle('#fff');
          ctx.setLineWidth(2);
          ctx.stroke();

          ctx.draw(false, () => {
            // 导出为临时图片
            wx.canvasToTempFilePath({
              canvasId: 'markerCanvas',
              quality: 0.1, // 降低质量（0-1，1为原图）
              success: tempRes => {
                console.warn('success', tempRes);
                // 保存临时路径
                const tempMarkerImages = { ...that.data.tempMarkerImages };
                tempMarkerImages[place.title] = tempRes.tempFilePath;
                that.saveTempPathToStorage(place.title, tempRes.tempFilePath)

                // 更新 marker 图标
                const markers = that.data.items2.map(m => {
                  if (m.title == place.title) {
                    return { ...m, iconPath: tempRes.tempFilePath };
                  }
                  return m;
                });

                that.setData({
                  items: markers,
                  items2: markers,
                  tempMarkerImages,
                  pendingMarkers: that.data.pendingMarkers.slice(1) // 移除已处理的 marker
                });
                // 继续处理下一个
                that.processMarkerQueue();
              },
              fail: err => {
                console.error('生成圆角图片失败', err);
                // 处理失败，继续下一个
                that.setData({
                  pendingMarkers: that.data.pendingMarkers.slice(1)
                });
                that.processMarkerQueue();
              }
            });
          })
        },
        fail: err => {
          console.error('加载图片失败', err);
          // 处理失败，继续下一个
          that.setData({
            pendingMarkers: this.data.pendingMarkers.slice(1)
          });
          that.processMarkerQueue();
        }
      });
    }
  },
  // 判断是否超过5天
  checkTimeDifference() {
    const startTime = wx.getStorageSync('startTime')
    if (!startTime) {return true}
    const timeDiffMs = Date.now() - startTime;
    const timeDiffDays = timeDiffMs / (1000 * 60 * 60 * 24);
    return timeDiffDays >= 5
  },
  hasCircle(title) {
    return !this.checkTimeDifference() && this.getCachedPath(title) != null
  },

  getMarker(place, index, x) {
    const newItem = {
      ...place,
      id: index,
      // id:Number(place.id),
      width: this.data.noNet ? 1 : 60,
      // width: this.data.noNet ? 1 : 45,
      height: this.data.noNet ? 1 : 45,
      // width: 50,
      // height: 37.5,
      // joinCluster: this.data.isJuhe ? (Number(place.rate) < 3) : false,
      joinCluster: false,
      alpha: 1,
      isFav: this.isFavorites(place),
      // iconPath: this.data.noNet ? '/images/m3.png' : place.img,
      iconPath: this.data.noNet ? '/images/m3.png' : this.hasCircle(place.title) ? this.getCachedPath(place.title) : place.img,
      collision: "poi",
      callout: {
        content: place.title,
        color: x == 1 ? '#555555' : (this.isFavorites(place) ? '#555555' : '#ffffff'),
        fontSize: 14,
        borderRadius: 3,
        bgColor: x == 1 ? (this.isFavorites(place) ? '#f8bfbf' : this.getColor(place)) :
          (this.isFavorites(place) ? '#f8bfbf' : '#2C8EF3'),
        padding: 8,
        display: 'ALWAYS',
        anchorX: this.hasCircle(place.title) ? -9 : 0,
        anchorY: -2,
        borderWidth: 0.5,
        // 不区分价格了
        // borderColor: place.price > 0 ? '#f89d9d' : '#7dacf7',
        borderColor: '#ffffff',
        // borderColor: x == 1 ? (this.isFavorites(place) ? '#f8bfbf' : this.getColor(place)) :
        //     (this.isFavorites(place) ? '#f8bfbf' : '#2C8EF3'),
      }
    }
    return newItem
  },
  onRegionChange(e) {
    // console.warn('onRegionChange')
    const { type, causedBy } = e.detail;
    // console.log('地图视野变化类型:', causedBy);
    // begin（开始拖动/缩放）、end（结束拖动/缩放）
    if (causedBy == 'gesture') {
      this.closeOverlay()
      this.setData({
        showTypeDropdown: false,
        showSettingDropdown: false,
        showSearchResult: false,
      })
    }
    // console.warn(e.detail)
    if (this.data.selectedName == '0全部') {
      if (e.type === 'end') {
        // console.warn(e.detail.region)
        const { northeast, southwest } = e.detail.region;
        if (northeast && southwest) {
          this.setData({
            viewport: { northeast, southwest }
          }, () => {
            this.checkMarkersInView(e.detail);
          });
        }
      }
      // this.checkMarkersInView(e.detail);
    } else if (this.data.selectedName == '3三星以上') {
      if (e.type === 'end') {
        const { northeast, southwest } = e.detail.region;
        this.setData({
          viewport: { northeast, southwest }
        }, () => {
          this.checkMarkersInView(e.detail);
        });
      }
      // this.checkMarkersInView(e.detail);
    }

  },
  calculateViewportBounds(latitude, longitude, scale) {
    const scaleFactor = 1 / scale;
    const latDelta = 0.5 * scaleFactor; // 根据经验值调整
    const lngDelta = 0.5 * scaleFactor / Math.cos(latitude * (Math.PI / 180)); // 考虑经纬度的转换

    const north = latitude + latDelta;
    const south = latitude - latDelta;
    const east = longitude + lngDelta;
    const west = longitude - lngDelta;

    return { north, south, east, west };
  },

  onShow() {
    this.processReq()
    this.setData({ noNet: getApp().globalData.noNet, video: getApp().globalData.video })
  },
  onLoad(options) {
    this.netInit()
    const globalData = getApp().globalData.myGlobalData.reverse()
    this.setData({
      items2: globalData
    });
    const favorites = wx.getStorageSync('favorites') || [];
    wx.setStorageSync('favorites', favorites)
    const t = wx.createMapContext('map', this)
    this.setData({ mapCtx: t })
    const allCache = wx.getStorageSync('markerCache') || {};
    console.log('allCache')
    console.log(allCache)
    this.setData({ cachedData: allCache })
    // this.clearMarkerCache()
  },
  clearMarkerCache() {
    wx.removeStorageSync('markerCache');
    this.setData({ cachedData: null });
    wx.showToast({
      title: '缓存已清除',
      icon: 'success'
    });
  },
  onImageLoad() {
    this.setData({
      hasLoaded: true
    });
  },
  getColor(item) {
    if (this.data.rateColor) {
      if (item.type.includes('v')) {
        return '#f7f799'
      }
      switch (Number(item.rate)) {
        case 5:
          return '#FBE0AE';
        case 4:
          return '#bdf4c6';
        case 3:
          return '#A8ECFC';
        case 2:
          return '#D7D0F6';
        case 1:
          return '#E0E1E1';
        case 0:
          return '#C0C0C0';
      }
    } else {
      return '#ffffff';
    }
  },
  onReady() {
    this.data.load = true;
    const { mapCtx } = this.data;
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

    mapCtx.initMarkerCluster({
      enableDefaultStyle: true, // false 关闭默认样式 true 使用默认样式
      zoomOnClick: true,
      gridSize: 20,
    });
  },
  initMarker() {
    const that = this
    const markers = this.data.items2.map((item, index) => that.getMarker(item, index, 1));
    return markers;
  },

  getLocalMarker(number) {
    const markers = this.initMarker();
    this.setData({
      items2: markers,
      selectedName: (number == 3) ? '3三星以上' : (number == 5) ? '0全部' : '-全部',
      // selectedName: '0全部',
      showTypeDropdown: false,
      showSettingDropdown: false,
    });
    this.closeOverlay()
  },
  getIcon(item) {
    switch (item.type[0]) {
      case 'a':
        return "/images2/m1.png"
      case 'b':
        return "/images2/m2.png"
      case 'c':
        return "/images2/m3.png"
      case 'd':
        return "/images2/m4.png"
      case 'f':
        return "/images2/m6.png"
      case 'g':
        return "/images2/m7.png"
      case 'h':
        return "/images2/m8.png"
      case 'i':
        return "/images2/m9.png"
    }
    return "/images2/m1.png"
  },
  selectType: function (e) {
    const index = e.currentTarget.dataset.index;
    // if (this.data.isJuhe == false) {
    //   this.setData({
    //     isJuhe: true,
    //   });
    //   const markers = this.initMarker();
    //   this.setData({
    //     items2: markers,
    //   });
    // }
    this.setData({
      selectedName: this.data.types[index],
      showTypeDropdown: false
    }, this.preFilter);
  },
  preFilter() {
    if (this.data.selectedName == '0全部') {
      this.setData({
        scale: this.data.scale - 0.5
      });
    } else {
      this.filterItems()
    }

  },

  selectSetting: function (e) {
    const index = e.currentTarget.dataset.index;
    if (this.data.setting[index] == '地图文字') {
      this.setData({
        showPoi: !this.data.showPoi,
        showSettingDropdown: false,
      });
    } else if (this.data.setting[index] == '点击锚定') {
      this.setData({
        showSettingDropdown: false,
        clickScale: !this.data.clickScale,
      });
    } else if (this.data.setting[index] == '圆形图标') {
      this.circleIcon()
    } else if (this.data.setting[index] == '清除图标') {
      this.setData({
        showSettingDropdown: false,
      });
      this.clearMarkerCache()
    } else if (this.data.setting[index] == '无网模式') {
      getApp().globalData.noNet = !this.data.noNet
      this.setData({ noNet: !this.data.noNet, showSettingDropdown: false, clickScale:false }, () => {
        this.getLocalMarker(5);
        // this.setData({
        //   longitude2: 116.397029,
        //   latitude2: 39.917839,
        // });
        this.filterItems()
        setTimeout(() => {
          const { mapCtx } = this.data;
          mapCtx.moveToLocation({
            longitude: 116.397029,
            latitude: 39.917839,
          });
          this.setData({
            scale: 14,
            longitude2: 116.397029,
            latitude2: 39.917839,
          });
        }, 1000)
      });

      // const markers = this.initMarker();
      this.closeOverlay()
      // this.setData({
      //   items2: markers,
      //   selectedName: '0全部',
      //   showTypeDropdown: false,
      //   showSettingDropdown: false,
      // }, this.filterItems);

    } else if (this.data.setting[index] == '评分底色') {
      this.setData({
        showSettingDropdown: false,
        rateColor: !this.data.rateColor,
      });
      const markers = this.initMarker();
      this.setData({
        items2: markers,
      }, this.filterItems);
    } else if (this.data.setting[index] == '切换聚合') {
      this.setData({
        isJuhe: !this.data.isJuhe,
        showSettingDropdown: false,
      });
      const markers = this.initMarker();
      this.setData({
        items2: markers,
      }, this.filterItems);
    } else if (this.data.setting[index] == '清除收藏') {
      this.setData({
        showSettingDropdown: false,
      });
      wx.showModal({
        title: '提示',
        content: '确定要清除所有收藏吗？',
        success: (res) => {
          if (res.confirm) {
            wx.removeStorageSync('favorites');
            wx.showToast({
              title: '收藏已清除',
              icon: 'success'
            });
          }
        }
      });
    } else if (this.data.setting[index] == '导出收藏') {
      const favorites = wx.getStorageSync('favorites') || [];
      const dataToExport = this.data.items2.filter(item => {
        return favorites.includes(item.title)
      }).map(item => {
        return item.title;
      });
      const dataString = JSON.stringify(dataToExport, null, 2);
      wx.setClipboardData({
        data: dataString,
        success(res) {
          wx.showToast({
            title: '复制成功',
            icon: 'success',
            duration: 2000
          });
        },
        fail(err) {
          wx.showToast({
            title: '复制失败',
            icon: 'none',
            duration: 2000
          });
          console.error('复制失败:', err);
        }
      });
    } else if (this.data.setting[index] == '自动播放') {
      this.setData({
        showSettingDropdown: false,
        autoVideo: !this.data.autoVideo,
        enableVideo: true,
      });
    } else if (this.data.setting[index] == '切换模式') {
      this.setData({
        showSettingDropdown: false,
        enableVideo: !this.data.enableVideo,
      });
      getApp().globalData.video = true
    } else if (this.data.setting[index] == '切换图标') {
      this.setData({
        showSettingDropdown: false,
        // icon: !this.data.icon,
        isJuhe: true,
      });
      const markers = this.initMarker();
      this.closeOverlay()
      this.setData({
        items2: markers,
        selectedName: '0全部',
        showTypeDropdown: false,
        showSettingDropdown: false,
      }, this.filterItems);
    }

  },
    circleIcon() {
        this.setData({
            showSettingDropdown: false,
        });
        const { items2, cachedData } = this.data;
        const items2Titles = [...new Set(items2.map(item => item.title))];
        // console.log('items2中的title列表：', items2Titles);
        let diffKeys
        let cacheKeys
        if (cachedData == null || cachedData.length == 0) {
            diffKeys = items2Titles
        } else {
            cacheKeys = Object.keys(cachedData);
            console.log('cachedData中的所有key：', cacheKeys);
            if (cacheKeys == null || cacheKeys.length == 0) {
                diffKeys = items2Titles
            } else {
                diffKeys = items2Titles.filter(key => !cacheKeys.includes(key));
            }
        }
        console.log('需要生成圆角的差异key：', diffKeys);
        this.setData({ unprocessedMarkers: items2.filter(obj => diffKeys.includes(obj.title)) })
        this.processMarkerQueue()
        wx.setStorageSync('startTime', Date.now());
    },
  getPrice(item) {
    if (item.price == '' || item.price == '0') {
      return 0
    } else {
      const firstPart = item.price.split('，')[0].replace('元', '')
      return Number(firstPart)
      // return '￥' + firstPart + '元'
    }
  },

  filterItems: function () {
    const { selectedName, items2 } = this.data;
    const filtered = items2.filter(item => {
      const typeMatch = Number(selectedName[0]) == 0 ||
        (selectedName[0] == '1' && item.price.includes('换票')) ||
        (selectedName[0] == '2' && (item.price.includes('一卡通') || item.recomend.includes('一卡通'))) ||
        (selectedName[0] == '3' && Number(item.rate) > 2) ||
        (selectedName[0] == '4' && item.type.includes('v')) ||
        (selectedName[0] == '5' && (wx.getStorageSync('favorites') || []).includes(item.title)) ||
        (selectedName[0] == '6' && item.rate != 0) ||
        (selectedName[0] == '7' && Number(item.rate) > 1) ||
        item.type.includes(selectedName[0]);
      return typeMatch;
    });
    const { mapCtx } = this.data;
    const markers = this.data.items;
    markers.forEach((marker) => {
      mapCtx.removeMarkers({
        markerIds: [marker.id]
      });
    });
    this.setData({
      items: []
    }, () => {
      // 计算每批加载的数量和总批次数
      const batchSize = Math.ceil(filtered.length / this.data.totalBatches);
      this.setData({
        batchSize,
        currentBatch: 1
      })
      this.loadBatchMarkers(filtered)
      //    this.setData({
      //      items: filtered
      //    })
      // this.setData({
      //     items: filtered
      // });
    });
    // const { mapCtx } = this.data;
    setTimeout(() => {
      const points = filtered.map(marker => ({
        latitude: marker.latitude,
        longitude: marker.longitude
      }));
      mapCtx.includePoints({
        points: points,
        // 上、右、下、左
        padding: [100, 50, 30, 50],
      });
    }, 500)

  },
  isFavorites(item: any) {
    const favorites = wx.getStorageSync('favorites') || [];
    return favorites.includes(item.title);
  },
  previewImage() {
    if (this.data.hasLoaded) {
      wx.previewImage({
        urls: this.data.hasVideo ? this.data.imgUrls.slice(1) : this.data.imgUrls,
        current: this.data.hasVideo ? this.data.current - 1 : this.data.current,
      });
    }
  },
  nav2() {
    const { mapCtx } = this.data;
    mapCtx.openMapApp({
      latitude: Number(this.data.overlayImage.latitude),
      longitude: Number(this.data.overlayImage.longitude),
      destination: this.data.overlayImage.title,
      name: this.data.overlayImage.title,
      address: this.data.overlayImage.title,
    });
  },
  toggleSwitch(e) {
    if (this.data.overlayImage.isFav) {
      this.removeFromFavorites(this.data.overlayImage.title);
    } else {
      this.addToFavorites(this.data.overlayImage.title);
    }
  },
  onSearchInput(e) {
    const keyword = e.detail.value.trim();
    if (keyword) {
      const result = this.data.items2.filter(item =>
        item.title.includes(keyword) || item.recomend.includes(keyword)
        || item.time.includes(keyword) || item.price.includes(keyword)
      );
      this.setData({
        showSearchResult: true,
        searchResult: result,
        showTypeDropdown: false,
        showSettingDropdown: false,
      });
      this.closeOverlay()
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
    selectedMarker.isFav = this.isFavorites(selectedMarker)
    this.setData({
      hasLoaded: false,
      address: '',
      overlayImage: selectedMarker,
      showSearchResult: false,
      searchResult: []
    });
    this.setData({
      longitude2: selectedMarker.longitude,
      latitude2: selectedMarker.latitude,
    });
    this.addMarkerNot(selectedMarker)
    this.imgPlay(selectedMarker)
  },
  addToFavorites(poiId) {
    // const poiId = event.currentTarget.dataset.poiId;
    const favorites = wx.getStorageSync('favorites') || [];
    if (!favorites.includes(poiId)) {
      favorites.push(poiId);
      wx.setStorageSync('favorites', favorites);
      console.log('已收藏POI:', poiId);
      wx.showToast({
        title: '收藏成功',
        icon: 'success'
      });
      this.setData({
        'overlayImage.isFav': true
      });
      const markerIndex = this.data.items.findIndex(marker => marker.title == poiId);
      const newMarkers = [...this.data.items];
      newMarkers[markerIndex].callout.fontSize = 14;
      newMarkers[markerIndex].callout.bgColor = this.isFavorites(newMarkers[markerIndex]) ? '#f8bfbf' : this.getColor(newMarkers[markerIndex]);
      newMarkers[markerIndex].callout.color = '#555555';
      newMarkers[markerIndex].isFav = this.isFavorites(newMarkers[markerIndex])
      this.setData({
        items: newMarkers
      });
      const markerIndex2 = this.data.items2.findIndex(marker => marker.title == poiId);
      const newMarkers2 = [...this.data.items2];
      newMarkers2[markerIndex2].callout.fontSize = 14;
      newMarkers2[markerIndex2].callout.bgColor = this.isFavorites(newMarkers[markerIndex2]) ? '#f8bfbf' : this.getColor(newMarkers[markerIndex2]);
      newMarkers2[markerIndex2].callout.color = '#555555';
      newMarkers2[markerIndex2].isFav = this.isFavorites(newMarkers2[markerIndex2])
      this.setData({
        items2: newMarkers2
      });
    } else {
      console.log('该POI ID已经收藏过了:', poiId);
    }
  },
  removeFromFavorites(poiId) {
    const favorites = wx.getStorageSync('favorites') || [];
    const index = favorites.indexOf(poiId);
    if (index !== -1) {
      favorites.splice(index, 1);
      wx.setStorageSync('favorites', favorites);
      this.setData({
        'overlayImage.isFav': false
      });
      console.warn(Number(this.data.selectedName[0]) == 5)
      if (Number(this.data.selectedName[0]) == 5) {
        console.warn(poiId)
        console.warn(this.data.items)
        const markerIndex = this.data.items.findIndex(marker => marker.title == poiId)
        console.warn(markerIndex)
        if (markerIndex !== -1) {
          const newItems = [...this.data.items];
          newItems.splice(markerIndex, 1);
          console.warn(newItems)
          this.setData({
            items: newItems
          });
        }
      } else {
        const markerIndex = this.data.items.findIndex(marker => marker.title == poiId);
        if (markerIndex !== -1) {
          const newMarkers = [...this.data.items];
          newMarkers[markerIndex].callout.fontSize = 14;
          newMarkers[markerIndex].callout.bgColor = this.isFavorites(newMarkers[markerIndex]) ? '#f8bfbf' : this.getColor(newMarkers[markerIndex]);
          newMarkers[markerIndex].callout.color = '#555555';
          newMarkers[markerIndex].isFav = this.isFavorites(newMarkers[markerIndex])
          this.setData({
            items: newMarkers
          });
        }
      }
      const markerIndex2 = this.data.items2.findIndex(marker => marker.title == poiId);
      if (markerIndex2 !== -1) {
        const newMarkers2 = [...this.data.items2];
        newMarkers2[markerIndex2].callout.fontSize = 14;
        newMarkers2[markerIndex2].callout.bgColor = this.isFavorites(newMarkers[markerIndex2]) ? '#f8bfbf' : this.getColor(newMarkers[markerIndex2]);
        newMarkers2[markerIndex2].callout.color = '#555555';
        newMarkers2[markerIndex2].isFav = this.isFavorites(newMarkers2[markerIndex2])
        this.setData({
          items2: newMarkers2
        });
      }
    }
  },
  onMapTap: function (e) {
    this.setData({
      showTypeDropdown: false,
      showSettingDropdown: false,
      showSearchResult: false,
      searchResult: [],
      showTop: !this.data.showTop
    })
    this.closeOverlay()
  },
  // 跳转过来的有bug，会加2个
  markerBigger(markerId) {
    const markerIndex = this.data.items.findIndex(marker => marker.id == markerId);
    if (markerIndex !== -1) {
      const newMarkers = [...this.data.items];
      newMarkers[markerIndex].callout.fontSize = 20;
      newMarkers[markerIndex].callout.bgColor = '#f56343';
      newMarkers[markerIndex].callout.color = '#fff';
      this.setData({
        items: newMarkers
      });
      setTimeout(() => {
        const newMarkers = [...this.data.items];
        newMarkers[markerIndex].callout.fontSize = 14;
        newMarkers[markerIndex].callout.bgColor = this.isFavorites(newMarkers[markerIndex]) ? '#f8bfbf' : this.getColor(newMarkers[markerIndex]);
        newMarkers[markerIndex].callout.color = '#555555';
        this.setData({
          items: newMarkers
        });
      }, 500);
    }
  },
  onMarkerTap(e) {
    const markerId = e.detail.markerId;
    console.warn(e)
    this.onMarkerTap2(markerId)
  },
  onMarkerTap2(markerId) {
    console.warn(markerId)
    this.markerBigger(markerId)
    let items4 = this.data.items;
    const marker = items4.find((m) => m.id == markerId);
    console.warn(marker)
    if (marker) {
      // this.setData({
      //   // scale: 14,
      //   longitude2: marker.longitude,
      //   latitude2: marker.latitude,
      // });
      this.setData({
        showSearchResult: false,
        searchResult: [],
        showTypeDropdown: false,
        showSettingDropdown: false,
      });
      marker.isFav = this.isFavorites(marker)
      if (marker.title == this.data.overlayImage.title) {
      } else {
        this.closeOverlay()
        this.setData({
          overlayImage: marker,
        });
        this.processTag(marker)
        this.getRgc(marker)
        this.imgPlay(marker)
        this.processDis(marker)
      }
      if (this.data.clickScale) {
        // this.goTo(marker)
        try {
          const res = wx.getSystemInfoSync();
          console.warn('res.platform:' + res.platform)
          if (res.platform == 'windows' || res.platform == 'mac') {
          } else if (res.platform == 'android' || res.platform == 'ios') {
            this.goTo(marker)
          }
        } catch (e) {
          console.error('获取系统信息失败：', e);
        }
      }
      this.processDis(marker)
    }
  },
  processDis(marker) {
    const allDistances = this.data.items2.map(poi => {
      const distance = this.getDistance(
        parseFloat(marker.latitude),
        parseFloat(marker.longitude),
        parseFloat(poi.latitude),
        parseFloat(poi.longitude)
      );
      return { ...poi, distance: distance.toFixed(2) }; // 保留2位小数
    });
    // console.warn("allDistances")
    // console.warn(allDistances)

    // 筛选并排序：排除自身，取距离最近的5个
    const nearbyPois = allDistances
      .filter(poi => poi.title != marker.title) // 排除当前点击的POI
      .sort((a, b) => a.distance - b.distance) // 按距离升序排序
      .slice(0, 4); // 取前5个

    // 打印或展示结果
    // console.warn("附近景点（前5名）：", nearbyPois)
    this.setData({ nearbyPois })
  },
  handleNearbyClick(e) {
    const marker = e.currentTarget.dataset.poi;
    this.setData({
      longitude2: marker.longitude,
      latitude2: marker.latitude,
    });
    marker.isFav = this.isFavorites(marker)

    this.closeOverlay()
    this.setData({
      overlayImage: marker,
    });
    this.processTag(marker)
    this.getRgc(marker)
    this.imgPlay(marker)
    this.processDis(marker)
  },
  openVideoPopup(e) {
    // 假设触控条高度为50px，可根据实际情况调整
    const controlBarHeight = 50;
    // 判断点击位置是否在触控条区域内
    console.warn(e.detail)
    console.warn(e.detail.y)
    const query = wx.createSelectorQuery();
    query.select('#video').boundingClientRect((rect) => {
      if (rect) {
        const top = rect.top;
        // console.log('video 组件的 top 值为:', top);
        console.warn(e.detail.y - top)
        if (e.detail.y - top > 110) {
          return;
        }
        this.setData({
          showVideoPopup: true,
        });
        setTimeout(() => {
          console.warn("currentTime:" + this.data.currentTime)
          const videoContext = wx.createVideoContext('popup-video');
          videoContext.seek(this.data.currentTime);
          videoContext.play();
        }, 500)
      }
    }).exec();

  },
  closeVideoPopup() {
    this.setData({
      showVideoPopup: false
    });
  },
  videoPlay() {
    this.setData({
      // 视频播放时，停止自动轮播
      autoplay: false
    });
  },
  videoPause() {
    this.setData({
      // 视频暂停时，继续自动轮播
      autoplay: !this.data.autoVideo
    });
  },
  videoTimeUpdate(e) {
    // 更新当前视频播放时间
    this.setData({
      currentTime: e.detail.currentTime
    });
  },
  imgPlay(marker) {
    const targetPoi = getApp().globalData.imgs.find((poi) => poi.title == marker.title);
    let newBannerImgs = []
    if (marker.video && marker.video != '' && this.data.enableVideo) {
      newBannerImgs = [marker.video].concat([marker.img])
      this.setData({ indicatorDots: true, autoplay: this.data.autoVideo ? false : true, hasVideo: true, popupVideoSrc: marker.video });
    } else {
      newBannerImgs = [marker.img]
      this.setData({ indicatorDots: false, autoplay: false, hasVideo: false, popupVideoSrc: '' });
    }
    this.setData({ hasVideoReal: marker.video && marker.video.length > 0, popupVideoSrc: marker.video });
    if (targetPoi) {
      newBannerImgs = newBannerImgs.concat(targetPoi.pics)
      this.setData({ indicatorDots: true, autoplay: (!(marker.video && marker.video != '')) || !this.data.autoVideo });
      // console.warn(newBannerImgs)
    }
    this.setData({
      imgUrls: newBannerImgs,

    });
    // console.warn(this.data.imgUrls)
  },
  log(x) {
    console.warn(x)
    wx.showToast({
      title: x
    })
  },
  goTo(place) {
    const { mapCtx } = this.data;
    mapCtx.moveToLocation({
      longitude: place.longitude,
      latitude: place.latitude,
    });
    setTimeout(() => {
      this.setData({
        scale: 14,
        longitude2: place.longitude,
        latitude2: place.latitude,
      });
    }, 250)

  },
  copyName2(title) {
    wx.setClipboardData({
      data: title,
    });
  },
  copyName() {
    wx.setClipboardData({
      data: this.data.overlayImage.title,
      success(res) {
        wx.showToast({
          title: '复制成功',
          icon: 'success',
          duration: 2000
        });
      },
      fail(err) {
        wx.showToast({
          title: '复制失败',
          icon: 'none',
          duration: 2000
        });
        console.error('复制失败:', err);
      }
    });
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

  getRgc(marker) {
    wx.request({
      url: 'https://restapi.amap.com/v3/geocode/regeo',
      data: {
        location: `${marker.longitude},${marker.latitude}`,
        key: "cf030e2b684785e9fee97ec94536efff"
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const addressInfo = res.data;

          try {
            const {
              province,
              district,
              township,
              streetNumber,
            } = addressInfo.regeocode.addressComponent;
            const fullAddress = `${province}${district}${township}${streetNumber.street}${streetNumber.number}`;
            // console.log(fullAddress);
            this.setData({
              address: fullAddress,
            });
          } catch (error) {
            this.setData({
              address: addressInfo.regeocode.formatted_address,
            });
          }
          // console.log('请求成功，地址信息：', addressInfo);
          // console.log('请求成功，地址信息2：', addressInfo.regeocode.formatted_address);
        } else {
          console.error('请求失败，状态码：', res.statusCode);
        }
      },
      fail: (err) => {
        console.error('请求出错：', err);
      }
    });

  },

  getRandomColor() {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  },
  isBeiJing() {
    return true;
    // return this.isLocationInRange(
    //   getApp().globalData.longitude, getApp().globalData.latitude, 116.4074, 39.9042, 150
    // )
  },
  location: function () {
    if (getApp().globalData.latitude > 10 && getApp().globalData.longitude > 10) {
      const res = { latitude: getApp().globalData.latitude, longitude: getApp().globalData.longitude }
      if (this.isBeiJing()) {
        this.goTo(res)
      }
    } else {
      const that = this;
      wx.getLocation({
        type: 'gcj02',
        success(res) {
          const latitude = res.latitude;
          const longitude = res.longitude;
          getApp().globalData.latitude = latitude
          getApp().globalData.longitude = longitude
          if (latitude && longitude) {
            if (that.isBeiJing()) {
              that.goTo(res)
            }
          }
        },
        fail(err) {
          this.log('定位失败，请开启定位权限')
          if (getApp().globalData.latitude > 0 && getApp().globalData.longitude > 0) {
            let res = {}
            res.latitude = getApp().globalData.latitude
            res.longitude = getApp().globalData.longitude
            if (that.isBeiJing()) {
              that.goTo(res)
            }
          }
        }
      });
    }
  },
  closeOverlay: function () {
    this.setData({
      tags: [],
      address: '',
      overlayImage: {},
      nearbyPois: [],
      hasLoaded: false,
      imgUrls: [],
      current: 0,
      autoplay: false,
      hasVideo: false,
      indicatorDots: false,
      circular: true,
      showVideoPopup: false,
      popupVideoSrc: '',
      currentTime: 0,
      hasVideoReal: false,
    });
  },

  xiaochengxu: function (e) {
    // this.closeOverlay();
    const appId = e.currentTarget.dataset.appid;
    wx.navigateToMiniProgram({
      appId: appId,
      extraData: {},
      envVersion: 'release',
      success(res) {
        // console.log('成功打开小程序', res);
      },
      fail(err) {
        // console.error('打开小程序失败', err);
      }
    });
  },

  biji: function (e) {
    // this.closeOverlay();
    const apptitle = e.currentTarget.dataset.apptitle;
    this.copyName2(apptitle)
    wx.navigateToMiniProgram({
      appId: "wx734c1ad7b3562129",
      envVersion: 'release',
    });
  },

  detail: function (e) {
    // this.closeOverlay();
    const apptitle = e.currentTarget.dataset.apptitle;
    this.copyName2(apptitle)
    wx.navigateToMiniProgram({
      appId: "wxde8ac0a21135c07d",
      path: "search/pages/before-search/before-search",
      envVersion: 'release',
    });
  },

  video: function (e) { this.setData({ showVideoPopup: true }) },

  toggleTypeDropdown: function () {
    this.closeOverlay()
    this.setData({
      showTypeDropdown: !this.data.showTypeDropdown,
      showSettingDropdown: false,
    });
  },
  toggleSettingDropdown: function () {
    this.closeOverlay()
    this.setData({
      showSettingDropdown: !this.data.showSettingDropdown,
      showTypeDropdown: false,
    });
  },
  change(e) {
    this.setData({
      current: e.detail.current
    });
  },
  getDistance(lat1, lon1, lat2, lon2) {
    const earthRadius = 6371; // 地球半径（公里）
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return earthRadius * c * 1000; // 距离（公里）
  },



  // exportDataToFile: function () {
  //   const fs = wx.getFileSystemManager();
  //   const dataToExport = this.data.items2.map(item => {
  //     const { callout, width, height, isFav, ...rest } = item;
  //     // 解构赋值，提取出callout并忽略
  //     return rest;
  //   });
  //   const dataString = JSON.stringify(dataToExport, null, 2);
  //   const filePath = `${wx.env.USER_DATA_PATH}/data.json`;
  //   fs.writeFile({
  //     filePath: filePath,
  //     data: dataString,
  //     encoding: 'utf8',
  //     success: res => {
  //       console.log('文件写入成功', res);
  //       wx.showToast({
  //         title: '导出成功',
  //         icon: 'success'
  //       });
  //     },
  //     fail: err => {
  //       console.error('文件写入失败', err);
  //       wx.showToast({
  //         title: '导出失败',
  //         icon: 'none'
  //       });
  //     }
  //   });
  // },


  // readDataFromFile: function () {
  //   const fs = wx.getFileSystemManager();
  //   const filePath = `${wx.env.USER_DATA_PATH}/data.json`;
  //   fs.readFile({
  //     filePath: filePath,
  //     encoding: 'utf8',
  //     success: res => {
  //       console.log('文件读取成功', res.data);
  //       const data = JSON.parse(res.data);
  //       this.setData({ items: data, items2: data, text: res.data });
  //       wx.showToast({
  //         title: '读取成功',
  //         icon: 'success'
  //       });
  //       this.goToNewPage(res.data)
  //     },
  //     fail: err => {
  //       console.error('文件读取失败', err);
  //       wx.showToast({
  //         title: '读取失败',
  //         icon: 'none'
  //       });
  //     }
  //   });
  // }

});
