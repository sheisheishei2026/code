<template>
  <view class="filter-page">
    <view :class="['filter-container', isShow ? 'show' : 'hide']">
      <view class="filter-trigger">
        <input placeholder="搜索地点" class="search-container-input" @input="onSearchInput" @click.stop="" :value="inputValue" />
      </view>
      <view class="filter-trigger" style="height: 40px;">
        <text style="border-radius: 5px; padding:5px; text-align: center;color: rgb(0, 0, 0);background-color: rgb(93, 107, 235);width:30%;color: rgb(253, 253, 253);" @click.stop="toggleSortPopup">{{sortOption}}</text>
        <text style="border-radius: 5px; padding:5px; text-align: center;color: rgb(9, 57, 129);background-color: rgb(9, 194, 148);margin-left:10px;margin-right:10px;width:30%;color: rgb(253, 253, 253);" @click.stop="goToMap">筛选景点分布</text>
        <text style="border-radius: 5px; padding:5px; color: rgb(3, 3, 3);text-align: center;background-color: rgb(26, 149, 231);width:30%;color: rgb(253, 253, 253);" @click.stop="toggleFilter">{{sliceFirst(selectedName)}}</text>
      </view>
      
      <view class="sort-popup" v-if="isSortPopupOpen" @touchmove.stop.prevent="" @click="closeSortPopup">
        <view class="sort-option" @click.stop="sortByPrice">按价格排序</view>
        <view class="sort-option" @click.stop="sortByDistance">按距离排序</view>
        <view class="sort-option" @click.stop="sortByRating">按评分排序</view>
        <view class="sort-option" @click.stop="sortByTime">按添加排序</view>
      </view>
      
      <view class="filter-options" v-if="isFilterOpen" @touchmove.stop.prevent="" @click="close">
        <view class="filter-column" style="color: rgb(59, 129, 235);">
          <view class="filter-option" v-for="(item, index) in newFilterOptions2" :key="index" @click.stop="selectNewFilterOption2(item)">
            {{item}}
          </view>
        </view>
        <view class="filter-column" style="color: rgb(13, 164, 184);">
          <view class="filter-option" v-for="(item, index) in newFilterOptions3" :key="index" @click.stop="selectNewFilterOption2(item)">
            {{item}}
          </view>
        </view>
        <view class="filter-column" style="color: rgb(13, 184, 22);">
          <view class="filter-option" v-for="(item, index) in newFilterOptions" :key="index" @click.stop="selectNewFilterOption2(item)">
            {{item}}
          </view>
        </view>
        <view class="filter-column" style="color: rgb(128, 114, 247);">
          <view class="filter-option" v-for="(item, index) in filterOptions" :key="index" @click.stop="selectFilterOption(item)">
            {{sliceFirst(item)}}
          </view>
        </view>
      </view>
    </view>

    <view class="outer">
      <scroll-view 
        :class="['list-container', isFilterOpen ? 'disabled-scroll' : '']"
        scroll-y="true"
        :scroll-top="scrollTop"
        @scroll="onListScroll"
        @touchstart="handleTouchStart" 
        @touchmove="handleTouchMove" 
        @touchend="handleTouchEnd"
        :lower-threshold="50"
        @scrolltolower="onReachBottom"
        :show-scrollbar="false"
      >
        <view class="list-item" v-for="(item, index) in placesList" :key="item.id || index" @click="onListItemClick(item)">
          <view class="triangle-label" v-if="need2(item)">
            <text>需预约</text>
          </view>

          <image class="item-image" mode="aspectFill" :src="noNet ? '' : item.img" :lazy-load="true"></image>
          <view class="item-content">
            <text class="item-title">{{item.title}}</text>
            <view class="tag-container" v-if="item.recomend && item.recomend !== ''">
              <view class="tag" v-for="(tag, tagIndex) in getTags(item)" :key="tagIndex">
                {{tag}}
              </view>
            </view>
            <text v-if="item.distance" class="item-price" style="margin-top: 3px;color:#4782ee">距离: {{item.distance}}km</text>
            <text class="item-price" style="margin-top: 3px; color: #db3838" v-if="item.price && item.price !== ''">价格: {{getPrice3(item)}}</text>
            
            <view class="star-rating" style="margin-top:3px;">
              <image v-for="n in 5" :key="n" :src="(n - 1) < Number(item.rate) ? '/static/images/star-filled.png' : '/static/images/star-empty.png'" mode="aspectFill"></image>
            </view>
          </view>
        </view>
        <view v-if="!hasMore" style="text-align: center; padding: 10px; color: #999;">没有更多数据了</view>
      </scroll-view>
    </view>
  </view>
</template>

<script>
import { mapState, mapWritableState } from 'pinia';
import { useGlobalStore } from '@/stores/global';

export default {
  data() {
    return {
      placesList: [],
      filterPlacesList: [],
      placesList2: [],
      isFilterOpen: false,
      isSortPopupOpen: false,
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
      lastScrollTop: 0,
      isShow: true,
      tabClickTime: 0,
      startY: 0,
      moveY: 0,
      touchStatus: false,
      threshold: 50,
      scrollTop: 0,
      noNet: false
    };
  },
  computed: {
    ...mapState(useGlobalStore, ['myGlobalData', 'noNet', 'latitude', 'longitude']),
    ...mapWritableState(useGlobalStore, ['selectedPlace', 'source', 'filterItem', 'navIds'])
  },
  onShow() {
    this.noNet = useGlobalStore().noNet;
  },
  onLoad() {
    this.selectedName = '0开启筛选';
    this.sortOption = '排序选项';
    this.sortedByRating = false;
    this.sortedByPrice = false;
    this.sortedByLocation = false;
    this.inputValue = '';
    
    this.onLoad2(false);
  },
  onPullDownRefresh() {
    this.onLoad();
    this.reload();
    uni.stopPullDownRefresh();
  },
  onTabItemTap(event) {
    const currentTime = new Date().getTime();
    if (currentTime - this.tabClickTime < 300) {
      this.scrollTop = 0;
      this.$nextTick(() => {
        this.scrollTop = 0.1;
      });
      this.isShow = true;
    }
    this.tabClickTime = currentTime;
  },
  methods: {
    onLoad2(reverse) {
      uni.showLoading({ title: "加载中" });
      const store = useGlobalStore();
      const globalData = store.myGlobalData ? JSON.parse(JSON.stringify(store.myGlobalData)) : [];
      
      this.placesList2 = reverse ? globalData.reverse() : globalData;
      
      // Get location if available
      const lat = store.latitude;
      const lon = store.longitude;
      
      if (lat > 0 && lon > 0) {
        const data = this.init(lat, lon);
        uni.hideLoading();
        this.placesList2 = data;
        this.filterPlacesList = data;
      } else {
        uni.hideLoading();
        this.filterPlacesList = this.placesList2;
      }
      this.loadData();
    },
    init(lat, lon) {
      if (lat && lon) {
        const earthRadius = 6371;
        return this.placesList2.map(place => {
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
      return this.placesList2;
    },
    loadData() {
      const { pageSize, currentPage, filterPlacesList } = this;
      const startIndex = (currentPage - 1) * pageSize;
      const endIndex = startIndex + pageSize;
      const newData = filterPlacesList.slice(startIndex, endIndex);
      
      if (newData.length < pageSize) {
        this.hasMore = false;
      }

      this.placesList = this.placesList.concat(newData);
      this.currentPage = currentPage + 1;
    },
    onReachBottom() {
      if (this.hasMore) {
        this.loadData();
      }
    },
    toggleFilter() {
      this.isFilterOpen = !this.isFilterOpen;
      this.isSortPopupOpen = false;
    },
    selectFilterOption(option) {
      this.selectedName = option;
      this.isFilterOpen = false;
      this.filterItem();
    },
    selectNewFilterOption2(option) {
      this.selectedName = 'Z' + option;
      this.isFilterOpen = false;
      this.filterItem();
    },
    filterItem() {
      this.clearInput();
      const { selectedName, placesList2 } = this;
      
      const filtered = placesList2.filter(item => {
        const typeMatch = Number(selectedName[0]) == 0 ||
            (selectedName[0] == '9' && item.time.includes('仅工作日')) ||
            (selectedName[0] == '8' && item.time.includes('预约')) ||
            (selectedName[0] == '7' && item.price.includes('60岁免费')) ||
            (selectedName[0] == '6' && item.time.includes('周日闭馆')) ||
            (selectedName[0] == '5' && item.time.includes('中午休息')) ||
            (selectedName[0] == '4' && (item.price.includes('一卡通') || item.recomend.includes('一卡通'))) ||
            (selectedName[0] == '3' && item.price.includes('换票')) ||
            (selectedName[0] == '2' && (this.getPriceVal(item) > 0)) ||
            (selectedName[0] == '1' && (this.getPriceVal(item) == 0)) ||
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
            (selectedName.includes('运动健身') && (item.recomend.includes('健身') || (item.recomend.includes('锻炼') || item.recomend.includes('体育')))) ||
            (selectedName.includes('公园年卡') && this.nianka.includes(item.title)) ||
            (selectedName.includes('夏日') && (item.recomend.includes('肌肉') || item.recomend.includes('游泳'))) ||
            (selectedName.includes('银杏') && (item.recomend.includes('秋天') || item.recomend.includes('银杏'))) ||
            (selectedName.includes('地铁直达') && (item.recomend.includes('地铁直达'))) ||
            (selectedName.includes('闲时可去') && (item.type.includes('x'))) ||
            (selectedName.includes('有公众号') && item.officalId != "") ||
            (selectedName.includes('有小程序') && item.appId != "") ||
            (selectedName.includes('我的收藏') && (uni.getStorageSync('favorites') || []).includes(item.title)) ||
            (selectedName.includes('三星以上') && Number(item.rate) > 2) ||
            (selectedName.includes('零星') && Number(item.rate) == 0) ||
            (selectedName.includes('一星') && Number(item.rate) == 1) ||
            (selectedName.includes('二星') && Number(item.rate) == 2) ||
            (selectedName.includes('三星') && Number(item.rate) == 3) ||
            (selectedName.includes('四五星') && (Number(item.rate) == 4 || Number(item.rate) == 5)) ||
            item.type.includes(selectedName[0]);
        return typeMatch;
      });

      if (this.sortOption == '按评分排序') {
        filtered.sort((a, b) => {
          return !this.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
        });
      } else if (this.sortOption == '按价格排序') {
        filtered.sort((a, b) => {
          return !this.sortedByPrice ? this.getPriceVal(a) - this.getPriceVal(b) : this.getPriceVal(b) - this.getPriceVal(a);
        });
      } else if (this.sortOption == '按距离排序') {
        filtered.sort((a, b) => (a.distance || 99999) - (b.distance || 99999));
      }

      this.filterPlacesList = filtered;
      this.reload();
    },
    getPriceVal(item) {
      if (!item.price || item.price == '' || item.price == '0') {
        return 0;
      } else {
        const firstPart = item.price.split('，')[0].replace('元', '');
        return Number(firstPart) || 0;
      }
    },
    resetHeader() {
      this.placesList = [];
      this.pageSize = 20;
      this.currentPage = 1;
      this.hasMore = true;
    },
    close() {
      this.isFilterOpen = false;
      this.isSortPopupOpen = false;
    },
    goToMap() {
      const places = this.filterPlacesList;
      let title;
      if (this.inputValue != '') {
        title = this.inputValue;
      } else if (this.selectedName == '0开启筛选') {
        title = '全部';
      } else {
        title = this.selectedName.slice(1);
      }
      const numberArray = places.map(p => p.title);
      
      this.filterItemStore = title; // Using intermediate variable if needed or direct store
      const store = useGlobalStore();
      store.filterItem = title;
      store.navIds = numberArray;
      store.source = 'filter';
      
      uni.switchTab({
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
      this.isSortPopupOpen = !this.isSortPopupOpen;
      this.isFilterOpen = false;
    },
    closeSortPopup() {
      this.isSortPopupOpen = false;
    },
    sortByTime() {
      this.sortOption = '按添加排序';
      this.sortedByRating = !this.sortedByRating;
      this.sortedByPrice = false;
      this.sortedByLocation = false;
      this.selectedName = '0开启筛选';
      this.onLoad2(true);
      this.reload();
    },
    sortByRating() {
      this.sortOption = '按评分排序';
      let list = [...this.filterPlacesList];
      list.sort((a, b) => {
        return this.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
      });
      this.sortedByRating = !this.sortedByRating;
      this.sortedByPrice = false;
      this.sortedByLocation = false;
      this.filterPlacesList = list;
      this.reload();
    },
    sortByPrice() {
      this.sortOption = '按价格排序';
      let list = [...this.filterPlacesList];
      list.sort((a, b) => {
        return this.sortedByPrice ? this.getPriceVal(a) - this.getPriceVal(b) : this.getPriceVal(b) - this.getPriceVal(a);
      });
      this.sortedByPrice = !this.sortedByPrice;
      this.sortedByRating = false;
      this.sortedByLocation = false;
      this.filterPlacesList = list;
      this.reload();
    },
    sortByDistance() {
      const store = useGlobalStore();
      if (store.latitude > 0 && store.longitude > 0) {
        this.sort(store.latitude, store.longitude);
      } else {
        uni.getLocation({
          type: 'gcj02',
          success: (res) => {
            store.latitude = res.latitude;
            store.longitude = res.longitude;
            this.sort(res.latitude, res.longitude);
          },
          fail: (err) => {
            console.error('获取位置失败', err);
            uni.showToast({
              title: '定位失败',
              icon: 'none'
            });
          }
        });
      }
    },
    sort(latitude, longitude) {
      const data = this.init(latitude, longitude);
      data.sort((a, b) => a.distance - b.distance);
      
      this.sortOption = '按距离排序';
      this.sortedByRating = false;
      this.sortedByPrice = false;
      this.sortedByLocation = true;
      
      this.placesList2 = data;
      this.filterPlacesList = data; // Important: update filtered list with sorted data
      this.reload();
    },
    onListScroll(e) {
      this.isSortPopupOpen = false;
      this.isFilterOpen = false;
      
      const scrollTop = e.detail.scrollTop;
      const lastScrollTop = this.lastScrollTop;
      
      if (scrollTop - lastScrollTop > 3) {
        this.isShow = false;
      } else if (scrollTop - lastScrollTop < -3) {
        this.isShow = true;
      }
      this.lastScrollTop = scrollTop;
    },
    onListItemClick(item) {
      this.close();
      const store = useGlobalStore();
      store.selectedPlace = item;
      store.source = 'filter';
      uni.switchTab({
        url: `/pages/map/map`,
        success: function (res) {
          console.log('跳转成功');
        },
        fail: function (err) {
          console.error('跳转失败:', err);
        }
      });
    },
    handleTouchStart(e) {
      const touch = e.changedTouches[0];
      this.startY = touch.clientY;
      this.touchStatus = true;
    },
    handleTouchMove(e) {
      if (!this.touchStatus) return;
      const touch = e.changedTouches[0];
      this.moveY = touch.clientY;
      
      const distance = this.startY - this.moveY;
      if (Math.abs(distance) > this.threshold) {
        if (distance > 3) {
          this.isShow = false;
          this.isFilterOpen = false;
          this.isSortPopupOpen = false;
        } else if (distance < -3) {
          this.isShow = true;
          this.isFilterOpen = false;
          this.isSortPopupOpen = false;
        }
      }
    },
    handleTouchEnd() {
      this.touchStatus = false;
    },
    onSearchInput(e) {
      const keyword = e.detail.value.trim();
      this.inputValue = keyword;
      this.selectedName = '0开启筛选';
      this.hasMore = false;
      
      if (keyword != '') {
        let result = this.placesList2.filter(item =>
          item.title.includes(keyword) || (item.recomend && item.recomend.includes(keyword))
          || (item.time && item.time.includes(keyword)) || (item.price && item.price.includes(keyword))
        );
        
        if (this.sortOption == '按评分排序') {
          result.sort((a, b) => {
            return !this.sortedByRating ? Number(a.rate) - Number(b.rate) : Number(b.rate) - Number(a.rate);
          });
        } else if (this.sortOption == '按价格排序') {
          result.sort((a, b) => {
            return !this.sortedByPrice ? this.getPriceVal(a) - this.getPriceVal(b) : this.getPriceVal(b) - this.getPriceVal(a);
          });
        } else if (this.sortOption == '按距离排序') {
          result.sort((a, b) => a.distance - b.distance);
        }
        
        this.filterPlacesList = result;
        this.placesList = result;
        this.backToTop();
      } else {
        this.filterPlacesList = this.placesList2;
        this.reload();
      }
    },
    clearInput() {
      this.inputValue = '';
    },
    reload() {
      this.resetHeader();
      this.loadData();
      this.backToTop();
    },
    backToTop() {
      this.scrollTop = 0;
      this.$nextTick(() => {
        this.scrollTop = 0.1;
      });
      this.isShow = true;
    },
    // Helpers
    sliceFirst(str) {
      return str ? str.slice(1) : '';
    },
    need2(place) {
      return place.time && place.time.indexOf('预约') !== -1;
    },
    getTags(place) {
      return place.recomend ? place.recomend.split('，') : [];
    },
    getPrice3(item) {
      if (item.price != '' && item.price != '0') {
        const firstPart = item.price.split('，')[0].replace('元', '');
        if (firstPart == 0) {
          return "免费";
        } else {
          return '￥' + firstPart + '元';
        }
      } else {
        return "免费";
      }
    }
  }
}
</script>

<style scoped>
.filter-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.list-container {
  width: 100vw;
  height: 100%;
  overflow-y: scroll;
  z-index: 2;
  background-color: white;
  padding-top: 5px;
}

.outer {
  flex: 1;
  width: 100vw;
  overflow: hidden;
  position: relative;
}

.play-icon {
  position: relative;
  top: -22%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 14px;
}

.list-item {
  position: relative;
  display: flex;
  min-height: 100px;
  margin: 0 8px 8px 8px;
  background-color: rgb(250, 250, 250);
  border-radius: 5px;
  overflow: hidden;
  align-items: stretch;
  box-shadow: 0 0 10px rgba(25, 25, 25, 0.2), 0 0 10px rgba(0, 0, 0, 0.1);
}

.item-image {
  width: 50%;
  height: auto;
  border-radius: 5px;
  object-fit: cover;
}

.item-content {
  width: 45%;
  padding: 8px;
  display: flex;
  margin-left: 5px;
  flex-direction: column;
  justify-content: center;
}

.item-title {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 16px;
  margin-bottom: 1px;
  margin-top: -1px;
}

.item-price {
  font-size: 12px;
  color: #666;
  font-weight: 430;
}

.star-rating {
  display: flex;
  justify-content: left;
  padding-right: 3px;
  margin-left: -2px;
}

.star-rating image {
  width: 15px;
  height: 15px;
  margin: 0 2px;
}

.show {
  display: block;
}

.hide {
  display: none;
}

.filter-container {
  position: relative;
  width: 100%;
  background-color: rgb(255, 255, 255);
  border-bottom: 1px solid #eee;
  z-index: 100;
}

.filter-trigger {
  padding-right: 8px;
  padding-left: 8px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 30px;
  font-size: 13px;
}

.filter-options {
  position: absolute;
  top: 71px;
  left: 0;
  width: 100vw;
  background-color: #fff;
  border-top: none;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 101;
  box-shadow: 0 10px 10px rgba(25, 25, 25, 0.4), 0 20px 20px rgba(0, 0, 0, 0.3), 0 30px 30px rgba(0, 0, 0, 0.2);
  display: flex;
  font-size: 14px;
  justify-content: space-between;
}

.filter-column {
  padding: 10px;
  justify-content: center;
}

.filter-option {
  padding: 5px;
  text-align: center;
}

.filter-option:hover {
  background-color: #f5f5f5;
}

.disabled-scroll {
  overflow: hidden;
}

.search-container-input {
  background-color: #ffffff;
  height: 28px;
  border: 1px solid #1978e6;
  flex-grow: 1;
  font-size: 13px;
  text-align: center;
  border-radius: 5px;
  z-index: 103;
}

.tag-container {
  display: flex;
  justify-content: left;
  flex-wrap: wrap;
  margin-right: 2px;
  margin-left: -1px;
  margin-bottom: -2px;
  margin-top: 2px;
}

.tag {
  padding: 2px 3px;
  border-radius: 4px; 
  margin-top: 1px;
  margin-bottom: 2px;
  margin-right: 2px;
  background-color: #f5f5f5;
  color: #585858;
  font-size: 10px;
  font-weight: 400;
  border: 1px solid rgb(41, 177, 231);
}

.sort-popup {
  position: absolute;
  top: 70px;
  left: 0px;
  width: 33%;
  text-align: center;
  background-color: #fff;
  border: 1px solid #eee;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
  z-index: 102;
  font-size: 14px;
  box-shadow: 0 10px 10px rgba(25, 25, 25, 0.4), 0 20px 20px rgba(0, 0, 0, 0.3), 0 30px 30px rgba(0, 0, 0, 0.2);
}

.sort-option {
  padding: 10px;
  border-bottom: 1px solid rgb(241, 241, 241);
}

.sort-option:last-child{
    border-bottom: none;
}

.sort-option:hover {
  background-color: #f5f5f5;
}

.triangle-label {
  position: absolute;
  bottom: 7px;
  right: 6px;
  width: 36px;
  height: 15px;
  background-color: #7412cf;
  border-radius: 4px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  z-index: 1;
}

.triangle-label text {
  color: white;
  font-size: 9px;
}
</style>
