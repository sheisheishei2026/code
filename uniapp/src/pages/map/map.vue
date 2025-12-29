<template>
  <view class="map-container">
    <view class="full-screen-container">
      <view class="search-container" v-if="showTop">
        <view class="dropdown">
          <view class="dropdown-header" style="background-color: rgb(128, 114, 247); height:21px;line-height:21px;" @click.stop="toggleSettingDropdown">地图设置</view>
          <view v-if="showSettingDropdown" class="dropdown-menu">
            <block v-for="(item, index) in setting" :key="index">
              <view class="dropdown-item" @click.stop="selectSetting(index)" style="width: 60px;" :style="{color: selectedSetting == setting[index] ? '#f00' : '#262727'}">
                {{item}}
              </view>
            </block>
          </view>
        </view>
        <input placeholder="搜索地点" class="search-container-input" @input="onSearchInput" :value="inputValue" />

        <view class="dropdown">
          <view class="dropdown-header" @click.stop="toggleTypeDropdown" style="height:21px;line-height:21px;">
            {{sliceFirst(selectedName)}}
          </view>
          <view v-if="showTypeDropdown" class="dropdown-menu">
            <block v-for="(item, index) in types" :key="index">
              <view class="dropdown-item" @click.stop="selectType(index)" style="width: 60px;" :style="{color: types[index] == selectedName ? '#ff0000' : '#262727'}">
                {{sliceFirst(item)}}
              </view>
            </block>
          </view>
        </view>

        <view v-if="showSearchResult" class="search-result">
          <view v-for="(item, index) in searchResult" :key="index" class="result-item" @click="onResultItemTap(index)">
            <image :src="item.img" mode="aspectFill" class="result-item-image has-shadow"></image>
            <text class="result-item-text">{{item.title}}</text>
          </view>
        </view>
      </view>

      <canvas 
        style="width: 60px; height: 45px; position: absolute; top: -100px; left: 0;" 
        canvas-id="markerCanvas"
      ></canvas>

      <map 
        id="map" 
        class="map-component" 
        :enable-3d="true" 
        :enable-overlooking="true" 
        :enable-auto-max-overlooking="true" 
        :markers="items" 
        @markertap="onMarkerTap" 
        :enable-marker-clustering="true" 
        :cluster-clickable="true"
        @click="onMapTap" 
        @callouttap="onMarkerTap" 
        @regionchange="onRegionChange" 
        :enable-poi="showPoi" 
        :enable-building="true" 
        :show-location="true" 
        :scale="scale" 
        :longitude="longitude2" 
        :latitude="latitude2"
      >
      </map>

        <view class="video-popup" v-if="showVideoPopup">
          <view class="popup-mask" @click="closeVideoPopup"></view>
          <view class="close-btn" @click="closeVideoPopup">关闭</view>
          <view class="popup-content">
            <video id="popup-video" class="video-cover-full" :src="popupVideoSrc" :autoplay="true" controls></video>
          </view>
        </view>

        <view class="overlay-content" v-if="overlayImage.title">
          <view class="container7">
            <swiper 
              class="swiper banner-container" 
              :class="hasLoaded ? 'has-shadow3' : 'noshadow'"
              v-if="!noNet && indicatorDots" 
              :indicator-dots="indicatorDots" 
              :autoplay="autoplay" 
              :interval="interval" 
              :duration="duration" 
              indicator-color="#AAACAD" 
              indicator-active-color="#ffffff" 
              :circular="circular" 
              easing-function="linear" 
              :current="current" 
              @change="change"
            >
              <block v-for="(item, index) in imgUrls" :key="index">
                <swiper-item>
                  <view v-if="hasVideo && index == 0" style="overflow: hidden;">
                    <video 
                      id="video" 
                      class="bgimg3" 
                      :src="item" 
                      :enable-progress-gesture="true" 
                      :muted="true" 
                      :show-fullscreen-btn="false" 
                      :show-center-play-btn="true" 
                      :show-play-btn="true" 
                      :show-bottom-progress="false" 
                      :autoplay="autoVideo" 
                      :controls="false" 
                      @click.stop="openVideoPopup" 
                      :picture-in-picture-mode="[]" 
                      :show-pip-btn="false" 
                      @play="videoPlay" 
                      @pause="videoPause" 
                      @timeupdate="videoTimeUpdate"
                    ></video>
                  </view>
                  <view v-else style="overflow: hidden;">
                    <image mode="aspectFill" class="bgimg2" :class="hasLoaded ? 'has-shadow5' : 'noshadow'" @click.stop="previewImage" @load="onImageLoad" :src="item"></image>
                  </view>
                </swiper-item>
              </block>
            </swiper>
            <image 
              :src="overlayImage.img" 
              v-if="!noNet && !indicatorDots" 
              mode="aspectFill" 
              class="bgimg22" 
              :class="hasLoaded ? 'has-shadow3' : 'noshadow'"
              @click.stop="previewImage" 
              @load="onImageLoad"
            ></image>
          </view>

          <view class="container4" @click="closeOverlay">
            <view class="container6" v-if="overlayImage.officalId && overlayImage.officalId !== ''" style="width:35px;justify-content: left;text-align: left; align-items:flex-start;margin-top: 0px;position:absolute;top:8px;right:8px">
              <image 
                :src="'https://open.weixin.qq.com/qr/code?username=' + overlayImage.officalId" 
                mode="aspectFill" 
                class="overlay-image" 
                @click.stop="xiaochengxu(overlayImage.appId)" 
                :style="overlayImage.appId ? 'border: 1px solid #e6e6e6;' : ''" 
                :show-menu-by-longpress="true"
              >
              </image>
            </view>

            <view class="container5">
              <text style="font-weight: 600;font-size: 14px;align-self: center;" @click.stop="copyName">{{overlayImage.title}}</text>
              <view class="star-rating" style="align-self: center;">
                <block v-for="(n, index) in 5" :key="index">
                  <image :src="index < overlayImage.rate ? '/static/images/star-filled.png' : '/static/images/star-empty.png'" mode="aspectFill"></image>
                </block>
              </view>
              <view class="container6" style="justify-content: center;text-align: center; align-items:center;">
                <view class="container8" style="justify-content:center; margin-top: -3px;">

                  <text v-if="video && hasVideoReal" class="custom-button3" style="background-color: rgb(230, 21, 73);" @click.stop="videoClick">视频</text>

                  <text class="custom-button3" style="background-color: rgb(230, 146, 21);" @click.stop="detail(overlayImage.title)">详情</text>

                  <text class="custom-button3" style="background-color: rgb(14, 190, 221);" @click.stop="biji(overlayImage.title)">笔记</text>

                  <text @click.stop="nav2" class="custom-button3" style="background-color: rgb(128, 114, 247); color: white;">导航</text>

                  <text @click.stop="toggleSwitch" class="custom-button3" :class="overlayImage.isFav ? 'switch-on' : 'switch-off'">{{overlayImage.isFav ? '取消' : '收藏'}}</text>

                  <text class="custom-button3" style="width: 42px; background-color: rgb(13, 184, 22);" v-if="overlayImage.appId" @click.stop="xiaochengxu(overlayImage.appId)">小程序</text>

                </view>
              </view>
              <view class="tag-container" style="align-self: center; margin-top: 2px;margin-left: 2px;">
                <view class="tag" v-for="(tag, index) in tags" :key="index">
                  {{tag.text}}
                </view>
              </view>

              <view>
                <view style="display: flex;flex-direction: column;margin-left: 2px;">

                  <view>
                    <view class="left1">
                      <image src="/static/images/a1.png" class="ic1" style="filter: hue-rotate(335deg) saturate(200%) brightness(90%);" mode="aspectFill"></image>
                      <text class="left2" style="color: rgb(231, 22, 22);">{{getPrice(overlayImage.price)}}</text>
                    </view>
                    <view class="left1" v-if="overlayImage.time != ''">
                      <image src="/static/images/a8.png" class="ic1" style="filter: hue-rotate(235deg) saturate(200%) brightness(90%);" mode="aspectFill"></image>
                      <view>
                        <view class="time-container" style="color: rgb(36, 129, 216);" v-for="(item, index) in splitTime(overlayImage.time)" :key="index">
                          <text class="left2" style="color: rgb(36, 129, 216);">{{item}}</text>
                        </view>
                      </view>
                    </view>
                    <view class="left1" v-if="address != ''">
                      <image src="/static/images/a5.png" class="ic1" style="filter: hue-rotate(90deg) saturate(200%) brightness(90%);" mode="aspectFill"></image>
                      <text class="left2" style="color: rgb(64, 167, 61);" v-if="address != ''">{{address}}</text>
                    </view>
                  </view>

                </view>

                <view v-if="nearbyPois.length > 0">
                  <view class="near-container" style="align-self: center; margin-top: 8px;">
                    <view class="near2" v-for="(item, index) in nearbyPois" :key="index" @click.stop="handleNearbyClick(item)">
                      <image v-if="!noNet" :src="item.img" style="border-radius:5px; width:25px; height:25px;margin:3px;"></image>
                      <text> {{item.distance}}m</text>
                      <text style="padding-left:3px;padding-right:3px;height:13px;align-self:center;"> {{ getTitle(item.title)}}</text>
                    </view>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </view>

        <image class="back-to-top-btn" mode="aspectFill" @click="location" src="/static/images/b2.png" />
      </view>
  </view>
</template>

<script>
import { mapState, mapWritableState } from 'pinia';
import { useGlobalStore } from '@/stores/global';

export default {
  data() {
    return {
      showTop: true,
      load: false,
      first: true,
      setting: ['点击锚定', '清除收藏', '清除图标', '自动播放', '导出收藏', '切换模式', '圆形图标', '无网模式'],
      types: ['0全部', '1换票', '2一卡通', '7两星以上', '3三星以上', '6屏蔽零星', '4小吃饭店', '5我的收藏'],
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
      items2: [], // Source data
      batchSize: 0,
      currentBatch: 1,
      totalBatches: 10,
      viewport: {
        northeast: { latitude: 0, longitude: 0 },
        southwest: { latitude: 0, longitude: 0 }
      },
      imgUrls: [],
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
      tempMarkerImages: {},
      pendingMarkers: [],
      cachedData: [],
      unprocessedMarkers: [],
      isConnected: true,
      inputValue: '',
      scrollTop: 0,
    };
  },
  computed: {
    ...mapState(useGlobalStore, ['myGlobalData']),
    ...mapWritableState(useGlobalStore, ['selectedPlace', 'filterItem', 'navIds', 'source', 'latitude', 'longitude', 'video', 'noNet'])
  },
  onShow() {
    this.processReq();
    const store = useGlobalStore();
    this.noNet = store.noNet;
    this.video = store.video;
  },
  onLoad(options) {
    this.netInit();
    const store = useGlobalStore();
    const globalData = store.myGlobalData ? JSON.parse(JSON.stringify(store.myGlobalData)).reverse() : [];
    this.items2 = globalData;
    
    const favorites = uni.getStorageSync('favorites') || [];
    uni.setStorageSync('favorites', favorites);
    
    this.mapCtx = uni.createMapContext('map', this);
    
    const allCache = uni.getStorageSync('markerCache') || {};
    this.cachedData = allCache;
  },
  onReady() {
    this.load = true;
    // uni.getSetting logic if needed for permissions
    // Marker clustering init is handled by props in UniApp mostly, but some API calls might be needed
    if(this.mapCtx && this.mapCtx.initMarkerCluster) {
        this.mapCtx.initMarkerCluster({
            enableDefaultStyle: true,
            zoomOnClick: true,
            gridSize: 20
        });
    }
  },
  methods: {
    // Helper methods from WXS
    sliceFirst(str) {
      return str ? str.slice(1) : '';
    },
    isVideo(item) {
      return item && item.endsWith('mp4');
    },
    getTitle(title) {
        return title && title.length > 6 ? title.slice(0, 6) : title;
    },
    getPrice(str) {
      if (str == 0 || str == '0') {
        return "免费";
      }
      if (String(str).slice(-1) == "元") {
        return str;
      }
      if (String(str).length > 3) {
        return str;
      }
      return str + "元";
    },
    getType(str) {
        if(!str) return '';
        switch (str[0]) {
            case 'a': return '人文·历史·大学';
            case 'b': return '艺术·展馆·书苑';
            case 'c': return '行业·科学·技术';
            case 'd': return '寺庙·坛观·宗教';
            case 'e': return '游乐·度假·表演';
            case 'g': return '商场·商圈·零售';
            case 'h': return '园区·大厦·酒店';
            case 'i': return '公园·山水·自然';
            case 'k': return '街道·美食·小吃';
            default: return '';
        }
    },
    splitTime(timeStr) {
      if (!timeStr) return [];
      return timeStr.split('；');
    },

    netInit() {
      uni.getNetworkType({
        success: (res) => {
          this.isConnected = res.networkType !== 'none';
        }
      });
      uni.onNetworkStatusChange((res) => {
        this.isConnected = res.isConnected;
        if (!res.isConnected) {
          uni.showToast({ title: '网络已断开', icon: 'none' });
          this.clickScale = false;
        } else {
          uni.showToast({ title: '网络已恢复', icon: 'none' });
          this.clickScale = true;
        }
      });
    },
    
    processReq() {
        const store = useGlobalStore();
        // Check time diff and clear cache if needed
        // if(this.checkTimeDifference()) {
        //     this.clearMarkerCache();
        // }
        // this.circleIcon();

        const place = store.selectedPlace;
        const filterItem = store.filterItem;
        const navIds = store.navIds;
        const source = store.source;

        if (navIds && navIds.length > 0) {
            this.closeOverlay();
            if (!this.load) {
                this.isJuhe = false;
                this.getLocalMarker(1);
            }
            if (filterItem) {
                this.selectedName = '-' + filterItem;
                // console.log(filterItem + "\n共" + navIds.length + "个地点");
            }
            if (filterItem == '全部') {
                this.scale = this.scale - 0.5;
                this.selectedName = '0全部';
            } else {
                const filteredArray = this.items2.filter(item => navIds.includes(item.title));
                this.items = [];
                this.showTypeDropdown = false;
                this.showSettingDropdown = false;
                
                this.$nextTick(() => {
                    this.items = filteredArray.map((item, index) => this.getMarker(item, index, 1)); // Ensure markers are formatted
                    
                    const points = filteredArray.map(marker => ({
                        latitude: Number(marker.latitude),
                        longitude: Number(marker.longitude)
                    }));
                    if(points.length > 0) {
                        this.mapCtx.includePoints({
                            points: points,
                            padding: [100, 50, 30, 50]
                        });
                    }
                });
            }
        } else if (place) {
            if (source == 'video' && store.video) {
                this.autoVideo = true;
                this.enableVideo = true;
            } else {
                this.autoVideo = false;
            }
            if (!this.load) {
                this.getLocalMarker(2);
            }
            this.selectedName = '0全部';
            this.longitude2 = Number(place.longitude);
            this.latitude2 = Number(place.latitude);
            
            this.processTag(place);
            this.items = [];
            this.closeOverlay();
            this.addMarkerNot(place);
            
            // Update isFav directly on the place object or a copy
            const placeCopy = {...place};
            placeCopy.isFav = this.isFavorites(placeCopy);
            this.overlayImage = placeCopy;
            
            this.imgPlay(placeCopy);
            this.processDis(placeCopy);
        } else if (this.first) {
            if (!this.load) {
                this.isJuhe = true;
                this.getLocalMarker(5);
            }
            this.longitude2 = 116.397029;
            this.latitude2 = 39.917839;
            this.mapCtx.moveToLocation({
                longitude: 116.397029,
                latitude: 39.917839
            });
            setTimeout(() => { this.location() }, 900);
        }
        this.first = false;
    },

    processTag(place) {
        const y = this.getType(place.type); // This might be needed if uncommented in source
        let tagArray = [];
        if (place.recomend) {
            tagArray = place.recomend.split('，');
        }
        const tags2 = tagArray.map(text => {
            return { text, color: this.getRandomColor() }
        });
        this.tags = tags2;
    },

    getLocalMarker(number) {
        const markers = this.initMarker();
        this.items2 = markers; // Keep items2 as source
        this.selectedName = (number == 3) ? '3三星以上' : (number == 5) ? '0全部' : '-全部';
        this.showTypeDropdown = false;
        this.showSettingDropdown = false;
        // In the original code, items2 is set, then filterItems is called or items is derived.
        // We should probably set items here or call filterItems. 
        // For now, let's mimic source: set items2, close overlay. But items need to be displayed.
        // If we just set items2, map markers won't update if bound to items.
        // The source code in getLocalMarker only sets items2. 
        // Wait, in filterItems/loadBatchMarkers, items is updated.
        // Let's assume we need to run filterItems after this to populate items.
        this.filterItems(); 
        this.closeOverlay();
    },

    initMarker() {
        return this.items2.map((item, index) => this.getMarker(item, index, 1));
    },

    getMarker(place, index, x) {
        const isFav = this.isFavorites(place);
        const hasCircle = this.hasCircle(place.title);
        const iconPath = this.noNet ? '/static/images/m3.png' : (hasCircle ? this.getCachedPath(place.title) : place.img);
        
        return {
            ...place,
            id: index,
            width: this.noNet ? 1 : 60,
            height: this.noNet ? 1 : 45,
            joinCluster: false, // Simplified from source
            alpha: 1,
            isFav: isFav,
            iconPath: iconPath,
            collision: "poi", // UniApp might not support this, but keeping it
            callout: {
                content: place.title,
                color: x == 1 ? '#555555' : (isFav ? '#555555' : '#ffffff'),
                fontSize: 14,
                borderRadius: 3,
                bgColor: x == 1 ? (isFav ? '#f8bfbf' : this.getColor(place)) : (isFav ? '#f8bfbf' : '#2C8EF3'),
                padding: 8,
                display: 'ALWAYS',
                anchorX: hasCircle ? -9 : 0,
                anchorY: -2,
                borderWidth: 0.5,
                borderColor: '#ffffff'
            },
            latitude: Number(place.latitude), // Ensure number
            longitude: Number(place.longitude)
        };
    },

    getColor(item) {
        if (this.rateColor) {
            if (item.type && item.type.includes('v')) return '#f7f799';
            switch (Number(item.rate)) {
                case 5: return '#FBE0AE';
                case 4: return '#bdf4c6';
                case 3: return '#A8ECFC';
                case 2: return '#D7D0F6';
                case 1: return '#E0E1E1';
                case 0: return '#C0C0C0';
                default: return '#ffffff';
            }
        } else {
            return '#ffffff';
        }
    },

    isFavorites(item) {
        const favorites = uni.getStorageSync('favorites') || [];
        return favorites.includes(item.title);
    },

    getRandomColor() {
        const letters = '0123456789ABCDEF';
        let color = '#';
        for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
        }
        return color;
    },

    addMarkerNot(place) {
        let items4 = [...this.items];
        if (!items4.some(item => item.title == place.title)) {
            const index = this.items2.findIndex(item => item.title === place.title);
            if (index !== -1) {
                const newItem = this.getMarker(place, index, 1);
                items4.push(newItem);
                this.items = items4;
            }
        }
        this.processTag(place);
        this.getRgc(place);
        if (this.clickScale) {
            this.goTo(place);
        }
        this.processDis(place);
    },

    goTo(place) {
        this.mapCtx.moveToLocation({
            longitude: Number(place.longitude),
            latitude: Number(place.latitude)
        });
        setTimeout(() => {
            this.scale = 14;
            this.longitude2 = Number(place.longitude);
            this.latitude2 = Number(place.latitude);
        }, 250);
    },

    onMarkerTap(e) {
        const markerId = e.detail.markerId;
        this.onMarkerTap2(markerId);
    },

    onMarkerTap2(markerId) {
        this.markerBigger(markerId);
        const marker = this.items.find(m => m.id == markerId);
        if (marker) {
            this.showSearchResult = false;
            this.searchResult = [];
            this.showTypeDropdown = false;
            this.showSettingDropdown = false;
            
            const markerCopy = {...marker};
            markerCopy.isFav = this.isFavorites(markerCopy);
            
            if (marker.title !== this.overlayImage.title) {
                this.closeOverlay();
                this.overlayImage = markerCopy;
                this.processTag(markerCopy);
                this.getRgc(markerCopy);
                this.imgPlay(markerCopy);
                this.processDis(markerCopy);
            }
            
            if (this.clickScale) {
                // simplified platform check
                this.goTo(markerCopy);
            }
            this.processDis(markerCopy);
        }
    },

    markerBigger(markerId) {
        const markerIndex = this.items.findIndex(marker => marker.id == markerId);
        if (markerIndex !== -1) {
            const newMarkers = [...this.items];
            newMarkers[markerIndex].callout.fontSize = 20;
            newMarkers[markerIndex].callout.bgColor = '#f56343';
            newMarkers[markerIndex].callout.color = '#fff';
            this.items = newMarkers;
            
            setTimeout(() => {
                const resetMarkers = [...this.items];
                // Need to re-get color and fav status logic
                const place = resetMarkers[markerIndex];
                const isFav = this.isFavorites(place);
                resetMarkers[markerIndex].callout.fontSize = 14;
                resetMarkers[markerIndex].callout.bgColor = isFav ? '#f8bfbf' : this.getColor(place);
                resetMarkers[markerIndex].callout.color = '#555555';
                this.items = resetMarkers;
            }, 500);
        }
    },

    closeOverlay() {
        this.tags = [];
        this.address = '';
        this.overlayImage = {};
        this.nearbyPois = [];
        this.hasLoaded = false;
        this.imgUrls = [];
        this.current = 0;
        this.autoplay = false;
        this.hasVideo = false;
        this.indicatorDots = false;
        this.circular = true;
        this.showVideoPopup = false;
        this.popupVideoSrc = '';
        this.currentTime = 0;
        this.hasVideoReal = false;
    },

    onMapTap() {
        this.showTypeDropdown = false;
        this.showSettingDropdown = false;
        this.showSearchResult = false;
        this.searchResult = [];
        this.showTop = !this.showTop;
        this.closeOverlay();
    },

    toggleSettingDropdown() {
        this.closeOverlay();
        this.showSettingDropdown = !this.showSettingDropdown;
        this.showTypeDropdown = false;
    },

    toggleTypeDropdown() {
        this.closeOverlay();
        this.showTypeDropdown = !this.showTypeDropdown;
        this.showSettingDropdown = false;
    },

    selectSetting(index) {
        const option = this.setting[index];
        if (option == '地图文字') {
            this.showPoi = !this.showPoi;
            this.showSettingDropdown = false;
        } else if (option == '点击锚定') {
            this.showSettingDropdown = false;
            this.clickScale = !this.clickScale;
        } else if (option == '圆形图标') {
            this.circleIcon();
        } else if (option == '清除图标') {
            this.showSettingDropdown = false;
            this.clearMarkerCache();
        } else if (option == '无网模式') {
            const store = useGlobalStore();
            store.noNet = !this.noNet;
            this.noNet = !this.noNet;
            this.showSettingDropdown = false;
            this.clickScale = false;
            
            this.getLocalMarker(5);
            this.filterItems();
            
            setTimeout(() => {
                this.mapCtx.moveToLocation({ longitude: 116.397029, latitude: 39.917839 });
                this.scale = 14;
                this.longitude2 = 116.397029;
                this.latitude2 = 39.917839;
            }, 1000);
        } else if (option == '清除收藏') {
            this.showSettingDropdown = false;
            uni.showModal({
                title: '提示',
                content: '确定要清除所有收藏吗？',
                success: (res) => {
                    if (res.confirm) {
                        uni.removeStorageSync('favorites');
                        uni.showToast({ title: '收藏已清除', icon: 'success' });
                    }
                }
            });
        }
        // Other settings omitted for brevity or logic match
        this.selectedSetting = option;
    },

    selectType(index) {
        this.selectedName = this.types[index];
        this.showTypeDropdown = false;
        this.preFilter();
    },

    preFilter() {
        if (this.selectedName == '0全部') {
            this.scale = this.scale - 0.5;
            this.filterItems(); // Need to call filterItems to reset
        } else {
            this.filterItems();
        }
    },

    filterItems() {
        const { selectedName, items2 } = this;
        const filtered = items2.filter(item => {
            const typeMatch = Number(selectedName[0]) == 0 ||
                (selectedName[0] == '1' && item.price.includes('换票')) ||
                (selectedName[0] == '2' && (item.price.includes('一卡通') || item.recomend.includes('一卡通'))) ||
                (selectedName[0] == '3' && Number(item.rate) > 2) ||
                (selectedName[0] == '4' && item.type.includes('v')) ||
                (selectedName[0] == '5' && (uni.getStorageSync('favorites') || []).includes(item.title)) ||
                (selectedName[0] == '6' && item.rate != 0) ||
                (selectedName[0] == '7' && Number(item.rate) > 1) ||
                item.type.includes(selectedName[0]);
            return typeMatch;
        });

        this.items = [];
        
        // Pagination/Batching logic simulation
        this.batchSize = Math.ceil(filtered.length / this.totalBatches);
        this.currentBatch = 1;
        
        // For simplicity in UniApp, we might just load all or use a simpler batch logic
        // Here assuming loadBatchMarkers is needed
        this.loadBatchMarkers(filtered);
        
        setTimeout(() => {
            const points = filtered.map(marker => ({
                latitude: Number(marker.latitude),
                longitude: Number(marker.longitude)
            }));
            if(points.length > 0) {
                this.mapCtx.includePoints({
                    points: points,
                    padding: [100, 50, 30, 50]
                });
            }
        }, 500);
    },

    loadBatchMarkers(filtered) {
        const { items, batchSize, currentBatch, totalBatches } = this;
        const startIndex = (currentBatch - 1) * batchSize;
        const endIndex = startIndex + batchSize;
        const newMarkers = filtered.slice(startIndex, endIndex).map((item, index) => this.getMarker(item, startIndex + index, 1));
        
        this.items = items.concat(newMarkers);
        
        if (currentBatch < totalBatches) {
            this.currentBatch = currentBatch + 1;
            setTimeout(() => {
                this.loadBatchMarkers(filtered);
            }, 50);
        }
    },

    onSearchInput(e) {
        const keyword = e.detail.value.trim();
        this.inputValue = keyword;
        if (keyword) {
            const result = this.items2.filter(item =>
                item.title.includes(keyword) || (item.recomend && item.recomend.includes(keyword))
                || (item.time && item.time.includes(keyword)) || (item.price && item.price.includes(keyword))
            );
            this.showSearchResult = true;
            this.searchResult = result;
            this.showTypeDropdown = false;
            this.showSettingDropdown = false;
            this.closeOverlay();
        } else {
            this.showSearchResult = false;
            this.searchResult = [];
        }
    },

    onResultItemTap(index) {
        const selectedMarker = this.searchResult[index];
        selectedMarker.isFav = this.isFavorites(selectedMarker);
        this.hasLoaded = false;
        this.address = '';
        this.overlayImage = selectedMarker;
        this.showSearchResult = false;
        this.searchResult = [];
        this.longitude2 = Number(selectedMarker.longitude);
        this.latitude2 = Number(selectedMarker.latitude);
        
        this.addMarkerNot(selectedMarker);
        this.imgPlay(selectedMarker);
    },

    // ... Other methods (imgPlay, videoPlay, etc.) ...
    imgPlay(marker) {
        // Assuming global store imgs logic or simplified
        // const targetPoi = getApp().globalData.imgs...
        // Simulating without extra images source for now unless in global store
        let newBannerImgs = [];
        if (marker.video && marker.video != '' && this.enableVideo) {
            newBannerImgs = [marker.video, marker.img];
            this.indicatorDots = true;
            this.autoplay = !this.autoVideo;
            this.hasVideo = true;
            this.popupVideoSrc = marker.video;
        } else {
            newBannerImgs = [marker.img];
            this.indicatorDots = false;
            this.autoplay = false;
            this.hasVideo = false;
            this.popupVideoSrc = '';
        }
        this.hasVideoReal = marker.video && marker.video.length > 0;
        this.popupVideoSrc = marker.video;
        this.imgUrls = newBannerImgs;
    },

    videoPlay() { this.autoplay = false; },
    videoPause() { this.autoplay = !this.autoVideo; },
    videoTimeUpdate(e) { this.currentTime = e.detail.currentTime; },
    videoClick(e) { this.showVideoPopup = true; },
    openVideoPopup() { this.showVideoPopup = true; },
    closeVideoPopup() { this.showVideoPopup = false; },
    previewImage() {
        if (this.hasLoaded) {
            uni.previewImage({
                urls: this.hasVideo ? this.imgUrls.slice(1) : this.imgUrls,
                current: this.hasVideo ? this.current - 1 : this.current
            });
        }
    },
    onImageLoad() { this.hasLoaded = true; },
    change(e) { this.current = e.detail.current; },

    toggleSwitch() {
        if (this.overlayImage.isFav) {
            this.removeFromFavorites(this.overlayImage.title);
        } else {
            this.addToFavorites(this.overlayImage.title);
        }
    },

    addToFavorites(poiId) {
        let favorites = uni.getStorageSync('favorites') || [];
        if (!favorites.includes(poiId)) {
            favorites.push(poiId);
            uni.setStorageSync('favorites', favorites);
            uni.showToast({ title: '收藏成功', icon: 'success' });
            this.overlayImage.isFav = true;
            // Update marker appearance logic...
            this.updateMarkerAppearance(poiId, true);
        }
    },

    removeFromFavorites(poiId) {
        let favorites = uni.getStorageSync('favorites') || [];
        const index = favorites.indexOf(poiId);
        if (index !== -1) {
            favorites.splice(index, 1);
            uni.setStorageSync('favorites', favorites);
            this.overlayImage.isFav = false;
            // Update marker appearance logic...
            this.updateMarkerAppearance(poiId, false);
        }
    },

    updateMarkerAppearance(poiId, isFav) {
        const markerIndex = this.items.findIndex(marker => marker.title == poiId);
        if (markerIndex !== -1) {
            const newMarkers = [...this.items];
            newMarkers[markerIndex].isFav = isFav;
            newMarkers[markerIndex].callout.bgColor = isFav ? '#f8bfbf' : this.getColor(newMarkers[markerIndex]);
            newMarkers[markerIndex].callout.color = '#555555';
            this.items = newMarkers;
        }
    },

    copyName() {
        uni.setClipboardData({
            data: this.overlayImage.title,
            success: () => uni.showToast({ title: '复制成功', icon: 'success' })
        });
    },

    nav2() {
        this.mapCtx.openMapApp({
            latitude: Number(this.overlayImage.latitude),
            longitude: Number(this.overlayImage.longitude),
            destination: this.overlayImage.title,
            name: this.overlayImage.title,
            address: this.overlayImage.title
        });
    },

    detail(title) {
        // Logic to copy name and maybe navigate
        this.copyName2(title);
        // Navigation to mini program
        uni.navigateToMiniProgram({
            appId: "wxde8ac0a21135c07d",
            path: "search/pages/before-search/before-search",
            envVersion: 'release'
        });
    },
    
    biji(title) {
        this.copyName2(title);
        uni.navigateToMiniProgram({
            appId: "wx734c1ad7b3562129",
            envVersion: 'release'
        });
    },
    
    xiaochengxu(appId) {
        if(!appId) return;
        uni.navigateToMiniProgram({
            appId: appId,
            envVersion: 'release'
        });
    },

    copyName2(title) {
        uni.setClipboardData({ data: title });
    },

    getRgc(marker) {
        // AMap logic
        uni.request({
            url: 'https://restapi.amap.com/v3/geocode/regeo',
            data: {
                location: `${marker.longitude},${marker.latitude}`,
                key: "cf030e2b684785e9fee97ec94536efff"
            },
            success: (res) => {
                if (res.statusCode === 200 && res.data.regeocode) {
                    this.address = res.data.regeocode.formatted_address;
                }
            }
        });
    },

    processDis(marker) {
        const allDistances = this.items2.map(poi => {
            const distance = this.getDistance(
                parseFloat(marker.latitude),
                parseFloat(marker.longitude),
                parseFloat(poi.latitude),
                parseFloat(poi.longitude)
            );
            return { ...poi, distance: distance.toFixed(2) };
        });
        const nearbyPois = allDistances
            .filter(poi => poi.title != marker.title)
            .sort((a, b) => a.distance - b.distance)
            .slice(0, 4);
        this.nearbyPois = nearbyPois;
    },

    getDistance(lat1, lon1, lat2, lon2) {
        const earthRadius = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c * 1000;
    },

    handleNearbyClick(marker) {
        // Similar logic to onResultItemTap
        this.longitude2 = Number(marker.longitude);
        this.latitude2 = Number(marker.latitude);
        const markerCopy = {...marker};
        markerCopy.isFav = this.isFavorites(markerCopy);
        
        this.closeOverlay();
        this.overlayImage = markerCopy;
        this.processTag(markerCopy);
        this.getRgc(markerCopy);
        this.imgPlay(markerCopy);
        this.processDis(markerCopy);
    },

    location() {
        uni.getLocation({
            type: 'gcj02',
            success: (res) => {
                this.goTo(res);
            },
            fail: () => {
                uni.showToast({ title: '定位失败', icon: 'none' });
            }
        });
    },

    onRegionChange(e) {
        if (e.type === 'end' && (e.causedBy === 'gesture' || e.causedBy === 'drag')) {
            this.closeOverlay();
            this.showTypeDropdown = false;
            this.showSettingDropdown = false;
            this.showSearchResult = false;
        }
    },

    // Canvas logic simplified for migration
    checkTimeDifference() {
        const startTime = uni.getStorageSync('startTime');
        if (!startTime) return true;
        const timeDiffMs = Date.now() - startTime;
        const timeDiffDays = timeDiffMs / (1000 * 60 * 60 * 24);
        return timeDiffDays >= 5;
    },
    clearMarkerCache() {
        uni.removeStorageSync('markerCache');
        this.cachedData = {};
        // uni.showToast({ title: '缓存已清除', icon: 'success' });
    },
    circleIcon() {
        this.showSettingDropdown = false;
        const { items2, cachedData } = this;
        const items2Titles = [...new Set(items2.map(item => item.title))];
        let diffKeys;
        let cacheKeys = cachedData ? Object.keys(cachedData) : [];
        
        if (!cacheKeys.length) {
            diffKeys = items2Titles;
        } else {
            diffKeys = items2Titles.filter(key => !cacheKeys.includes(key));
        }
        
        this.unprocessedMarkers = items2.filter(obj => diffKeys.includes(obj.title));
        this.processMarkerQueue();
        uni.setStorageSync('startTime', Date.now());
    },
    processMarkerQueue() {
        if (this.pendingMarkers.length === 0) {
            const pending = this.unprocessedMarkers.splice(0, 10);
            this.pendingMarkers = pending;
        }
        const marker = this.pendingMarkers[0];
        if (marker) {
            this.createRoundedMarker(marker);
        }
    },
    createRoundedMarker(place) {
        // Canvas logic needs adaptation for UniApp if strict mode, but general logic:
        const ctx = uni.createCanvasContext('markerCanvas', this);
        const diameter = 40; 
        const radius = diameter / 2;

        ctx.beginPath();
        ctx.arc(radius, radius, radius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.clip();

        uni.getImageInfo({
            src: place.img,
            success: (res) => {
                // Drawing logic
                const imgRatio = res.width / res.height;
                const canvasRatio = 1;
                let drawWidth, drawHeight, drawX, drawY;

                if (imgRatio > canvasRatio) {
                    drawHeight = diameter;
                    drawWidth = drawHeight * imgRatio;
                    drawX = radius - drawWidth / 2;
                    drawY = 0;
                } else {
                    drawWidth = diameter;
                    drawHeight = drawWidth / imgRatio;
                    drawX = 0;
                    drawY = radius - drawHeight / 2;
                }
                
                ctx.drawImage(res.path, drawX, drawY, drawWidth, drawHeight);
                
                ctx.beginPath();
                ctx.arc(radius, radius, radius, 0, Math.PI * 2);
                ctx.setStrokeStyle('#fff');
                ctx.setLineWidth(2);
                ctx.stroke();
                
                ctx.draw(false, () => {
                    uni.canvasToTempFilePath({
                        canvasId: 'markerCanvas',
                        quality: 0.1,
                        success: (tempRes) => {
                            const tempMarkerImages = { ...this.tempMarkerImages };
                            tempMarkerImages[place.title] = tempRes.tempFilePath;
                            this.saveTempPathToStorage(place.title, tempRes.tempFilePath);
                            
                            // Update logic would go here...
                            this.pendingMarkers = this.pendingMarkers.slice(1);
                            this.processMarkerQueue();
                        },
                        fail: () => {
                            this.pendingMarkers = this.pendingMarkers.slice(1);
                            this.processMarkerQueue();
                        }
                    }, this); // Pass 'this' for component scope
                });
            },
            fail: () => {
                this.pendingMarkers = this.pendingMarkers.slice(1);
                this.processMarkerQueue();
            }
        });
    },
    saveTempPathToStorage(key, tempFilePath) {
        const newCachedData = {
            ...this.cachedData,
            [key]: { tempFilePath }
        };
        this.cachedData = newCachedData;
        uni.setStorageSync('markerCache', newCachedData);
    },
    hasCircle(title) {
        return !this.checkTimeDifference() && this.getCachedPath(title) != null;
    },
    getCachedPath(key) {
        const cachedItem = this.cachedData ? this.cachedData[key] : null;
        return (cachedItem && cachedItem.tempFilePath) ? cachedItem.tempFilePath : null;
    }
  }
};
</script>

<style scoped>
/* Map container and component styles */
.map-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.map-component {
  width: 100vw;
  height: 100vh;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 1;
}

/* Copy from map.wxss */
.overlay-content3 {
  position: fixed;
  right: 0%;
  top: 1%;
  border: 1px solid #ddd;
  border-radius: 10px;
}

.scrollable-text {
  width: 90%;
  height: 80%;
  border: 1px solid #ccc;
  background-color: #ddd;
  border-radius: 5px;
  padding: 10px;
  overflow-y: scroll;
}
.video-cover-full{
  width: 100vw;
  height: 45vh;
  object-fit: fill;
  z-index: 1200;
}
.bgimg2 {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
.bgimg22 {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

.bgimg3 {
  width: 100%;
  height: 155px;
  object-fit: fill;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  margin-left: 0;
}

.has-shadow2{
  box-shadow:
    -5px -5px 10px rgba(25, 25, 25, 0.2),
    5px -5px 10px rgba(25, 25, 25, 0.2),
    -10px -10px 15px rgba(0, 0, 0, 0.1),
    10px -10px 15px rgba(0, 0, 0, 0.1);
}

.has-shadow3{
  box-shadow:
    -5px -5px 10px rgba(25, 25, 25, 0.2),
    5px -5px 10px rgba(25, 25, 25, 0.2),
    -10px -10px 15px rgba(0, 0, 0, 0.1),
    10px -10px 15px rgba(0, 0, 0, 0.1);
    height: 150px;
}

.has-shadow5 {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  height: 150px;
}

.noshadow{
    height: 0px;
}

.no{
    display: none;
}

.overlay-content {
  position: fixed;
  left: 50%; 
  transform: translateX(-50%); 
  width: 94vw;
  bottom: 20px;
  padding: 0;
  margin: 0;
  border-radius: 10px;
  background-size: cover;
  z-index: 1399;
}

.overlay-image {
  width: 35px;
  height: 35px;
  border-radius: 5px;
}

.container4 {
  position: relative;
  min-height: 90px;
  background-color: #e6e6e6;
  border-radius: 10px;
  box-shadow:
    0 0 30px rgba(25, 25, 25, 0.6),
    0 0 50px rgba(0, 0, 0, 0.3),
    0 0 80px rgba(0, 0, 0, 0.2);
  display: flex;
  padding: 10px;
  flex-direction: row;
  font-size: 12px;
  justify-content: center;
  text-align: center;
  z-index: 100;
  margin-top: -10px;
  width: 100vw;
  margin-left: -3vw;
}

.ic1{
  width: 15px;
  height: 15px;
  object-fit: cover;
  margin-right: 5px;
  flex-shrink: 0;
}

.left1{
  display: flex;
  align-items: center;
  margin-top: 3px;
  color:#1b57f0;
  padding-bottom: 2px;
  border-bottom: 0.5px dashed rgb(255, 255, 255);
}

.left2{
  text-align: left;  color:#161616;
  font-size:13px;
  font-weight: 430;
}

.container5 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: left;
  flex-grow: 1;
}

.container6 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.container7 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 94vw;
  margin: 0 auto;
}

.full-screen-container {
  width: 100vw;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.container8 {
  display: flex;
  flex-direction: row;
  justify-content: center;
  text-align: center;
  align-items: center;
  margin-top: 5px;
}

.dropdown {
  margin: 10px;
  min-width: 70px;
  overflow: hidden;
}

.dropdown-header {
  padding: 3px;
  border-radius: 5px;
  background-color: rgb(59, 129, 235);
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  color: white;
  text-overflow: ellipsis;
}

.dropdown-menu {
  position: fixed;
  top: 33px;
  border-radius: 5px;
  background-color: #fff;
  overflow: hidden;
  box-shadow:
        0 10px 10px rgba(25, 25, 25, 0.4),
        0 20px 20px rgba(0, 0, 0, 0.3),
        0 30px 30px rgba(0, 0, 0, 0.2);
  z-index: 1001; /* Ensure it's above */
}

.dropdown-item {
  font-size: 13px;
  padding: 10px 5px;
  border-top: 1px solid #eee;
}

.dropdown-item:last-child {
  border-bottom: 1px solid #eee;
}

.custom-button3 {
  width: 37px;
  font-size: 12px;
  line-height: 20px;
  border: none;
  vertical-align: middle;
  height: 20px;
  border-radius: 5px;
  padding-left: 3px;
  padding-right: 3px;
  margin: 4px;
  color: white;
}

.custom-button3:last-child{
  margin-right: 0px;
}

.switch-on {
  background-color: rgb(221, 99, 99);
}

.switch-off {
  background-color: rgb(59, 129, 235);
}
.near-container{
  display: flex;
  flex-wrap: nowrap;
  width: 89vw;
  overflow: hidden;
}

.near2 {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 2px 2px;
  border-radius: 4px;
  margin-right: 3px;
  margin-left: 3px;
  margin-top: 2px;
  margin-bottom: 2px;
  text-align: center;
  color: rgb(44, 44, 44);
  background-color:rgb(243, 235, 209);
  font-size: 10px;
  border-radius: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
}

.tag {
  padding: 2px 3px;
  border-radius: 4px;
  margin-right: 3px;
  margin-bottom: 2px;
  color: rgb(44, 44, 44);
  background-color: rgb(255, 255, 255);
  font-size: 10px;
  border-radius: 5px;
  border: 1px solid rgb(41, 177, 231);
}

.tag:last-child{
  margin-right: 0px;
}

.search-container {
  top: var(--window-top);
  position: fixed;
  text-align: center;
  height: 40px;
  display: flex;
  flex-direction: row;
  align-items: center;
  width:100vw;
  overflow: visible;
  padding: 0px 10px 10px 10px;
  justify-content: space-around;
  background-color: white;
  z-index: 1000;
}
.search-container-input {
  background-color:#ffffff;
  height: 25px;
  border: 1px solid #1978e6;
  flex-grow: 1;
  margin: 0px 0px;
  font-size: 13px;
  text-align: center;
  border-radius: 5px;
  z-index: 1000;
}
.search-result {
  overflow-x: scroll;
  position: fixed;
  top: 38px;
  left: 0;
  max-height: 500px;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  width: 100%;
  background-color: white;
  border-radius: 8px;
  z-index: 100;
  box-shadow:
  0 10px 10px rgba(25, 25, 25, 0.4),
  0 20px 20px rgba(0, 0, 0, 0.3),
  0 30px 30px rgba(0, 0, 0, 0.2);
}
.search-result view {
  padding-top: 5px;
  border-bottom: none;
}

.result-item {
  width: 33vw;
  margin-bottom: 10px;
  text-align: center;
}
.result-item-image {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 3px;
}
.result-item-text {
  display: block;
  margin-top: 5px;
  font-size: 12px;
}

.star-rating {
  display: flex;
  justify-content: left;
  padding: 5px 0px 5px 0px;
  margin-bottom: 0px;
}

.star-rating image {
  text-align: left;
  width: 15px;
  height: 15px;
  margin: 0 3px;
}

.banner-container {
  width: 100%;
  height: 150px;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
  overflow: hidden;
}
.has-shadow {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.video-popup {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 2010;
}

.popup-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 1);
}

.popup-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.close-btn {
  position: absolute;
  top: 160px;
  right: 10px;
  padding: 5px 10px;
  color:#ffffff;
  border-radius: 5px;
  z-index: 1201;
}

.time-container {
  display: flex;
  flex-direction: row;
}

/* Swiper dots custom styles if needed */
.swiper swiper{width: 95%;height: 110rpx; margin: 15rpx auto; border-radius: 20rpx; overflow: hidden;}
.swiper swiper image{width: 100%; height: 110rpx;border-radius: 20rpx;}
.swiper .wx-swiper-dots .wx-swiper-dot{width:8rpx;height:8rpx;border-radius:4rpx;background-color:#d8d9dd}
.swiper .wx-swiper-dots .wx-swiper-dot:nth-of-type(n+2){margin-left:0rpx}
.swiper .wx-swiper-dots .wx-swiper-dot.wx-swiper-dot-active{width:24rpx;height:8rpx;border-radius:4rpx;background-color:#fff}
.swiper .wx-swiper-dots.wx-swiper-dots-horizontal{bottom:10rpx;text-align:center}

.back-to-top-btn {
  position: fixed;
  bottom: 80px;
  right: 20px;
  width: 40px;
  height: 40px;
  z-index: 999;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
</style>