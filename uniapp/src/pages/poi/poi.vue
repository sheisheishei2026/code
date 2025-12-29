<template>
  <scroll-view 
    id="scroll" 
    :enable-passive="true" 
    :show-scrollbar="false" 
    scroll-with-animation="true" 
    :scroll-top="scrollTop"
    :refresher-enabled="true"
    :refresher-triggered="refresherTriggered"
    @refresherrefresh="onRefresherRefresh"
    style="height: 100vh; overflow: hidden;" 
    scroll-y="true"
  >
    <view v-for="(group, index) in groupedData" :key="index" class="group">
      <view class="group-title" @click="toggleGroup(index)">
        <text @click.stop="onPlaceTap2(group)" style="font-size: 16px; color: #1769e4;">{{group.groupName}}</text>
        <text style="padding-left:0px; font-size: 13px; color: #911ae0;">{{group.expanded ? '收起' : '展开'}}</text>
      </view>
      
      <view v-if="group.expanded" class="group-items">
        <block v-for="(place, placeIndex) in group.items" :key="place.id || placeIndex">
          <view v-if="placeIndex % 2 === 0" class="item">
            <!-- First Item -->
            <view :id="'place_' + index + '_' + placeIndex" class="place" style="margin-right: 5px;" @click="onPlaceTap(place)">
              <image :src="noNet ? '' : place.img" mode="aspectFill" :lazy-load="true"></image>
              <view class="play-icon" v-if="video && place.video"></view>
              <text class="single">{{place.title}}</text>
              <view class="star-rating">
                <image v-for="n in 5" :key="n" :src="(n - 1) < place.rate ? '/static/images/star-filled.png' : '/static/images/star-empty.png'" mode="aspectFill"></image>
              </view>
            </view>
            
            <!-- Second Item -->
            <view 
              :id="'place_' + index + '_' + (placeIndex + 1)" 
              v-if="placeIndex + 1 < group.items.length" 
              class="place" 
              style="margin-left: 5px;" 
              @click="onPlaceTap(group.items[placeIndex + 1])"
            >
              <image :src="noNet ? '' : group.items[placeIndex + 1].img" mode="aspectFill" :lazy-load="true"></image>
              <view class="play-icon" v-if="video && group.items[placeIndex + 1].video"></view>
              <text class="single">{{group.items[placeIndex + 1].title}}</text>
              <view class="star-rating">
                <image v-for="n in 5" :key="n" :src="(n - 1) < group.items[placeIndex + 1].rate ? '/static/images/star-filled.png' : '/static/images/star-empty.png'" mode="aspectFill"></image>
              </view>
            </view>
            
            <!-- Spacer if odd number of items -->
            <view v-else class="place" style="margin-left: 5px; visibility: hidden; border: none; box-shadow: none;"></view>
          </view>
        </block>
      </view>
    </view>
  </scroll-view>
  
  <image class="back-to-top-btn" style="box-sizing:border-box; padding:5px" @click="toggleEx" src="/static/images/zhedie.png" />
</template>

<script>
import { mapState, mapWritableState } from 'pinia';
import { useGlobalStore } from '@/stores/global';

export default {
  data() {
    return {
      groupedData: [],
      types: [
        'a人文·历史·建筑·大学', 
        'b艺术·展馆·书苑·剧院', 
        'c行业·科学·技术·专业', 
        'd寺庙·坛观·宗教·教堂',
        'e游乐·度假·表演·小镇', 
        'g商场·商圈·零售·百货', 
        'h园区·大厦·酒店·建筑', 
        'i公园·山水·自然·美景', 
        'k胡同·交通·美食·其他'
      ],
      da: [],
      currentGroupIndex: 0,
      scrollTop: 0,
      tabClickTime: 0,
      noNet: false,
      video: false,
      refresherTriggered: false
    };
  },
  computed: {
    ...mapState(useGlobalStore, ['myGlobalData', 'noNet', 'video']),
    ...mapWritableState(useGlobalStore, ['selectedPlace', 'source', 'filterItem', 'navIds'])
  },
  onShow() {
    const store = useGlobalStore();
    this.noNet = store.noNet;
    this.video = store.video;
  },
  onLoad() {
    uni.showLoading({ title: '加载数据中...' });
    const store = useGlobalStore();
    this.da = store.myGlobalData;
    this.groupData();
  },
  onTabItemTap(event) {
    const currentTime = new Date().getTime();
    if (currentTime - this.tabClickTime < 300) {
      this.scrollTop = 0.1;
      this.$nextTick(() => {
        this.scrollTop = 0;
      });
    }
    this.tabClickTime = currentTime;
  },
  methods: {
    onRefresherRefresh() {
      this.refresherTriggered = true;
      setTimeout(() => {
        this.refresherTriggered = false;
      }, 500);
    },
    groupData() {
      const originalData = this.da;
      const types = this.types;
      const groupNameCache = {};
      const grouped = {};
      
      originalData.forEach(item => {
        // Assume item.type is a string or array. The source uses item.type[0]
        // If item.type is "ab", item.type[0] is "a".
        const groupName = item.type && item.type.length > 0 ? item.type[0] : 'k';
        
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

      // Sort items by rate
      for (const group in grouped) {
        grouped[group].items.sort((a, b) => {
          return (b.rate || 0) - (a.rate || 0);
        });
        const itemCount = grouped[group].items.length;
        grouped[group].groupName = `${grouped[group].groupName} (${itemCount})`;
      }
      
      // Convert to array
      const groupedDataArray = Object.values(grouped);
      // Sort groups based on type order logic if needed, but here Object.values might be arbitrary.
      // Usually it's better to sort groupedDataArray by the index of type in types array.
      groupedDataArray.sort((a, b) => {
         const typeA = this.getTypeChar(a.groupName);
         const typeB = this.getTypeChar(b.groupName);
         return typeA.localeCompare(typeB);
      });

      this.currentGroupIndex = 0;
      this.groupedData = [];
      this.loadNextGroup(groupedDataArray);
    },
    getTypeChar(fullGroupName) {
        // Extract the first char if it matches types logic, but wait, groupName now has count like "a人文... (5)"
        // The type char is likely at the start.
        return fullGroupName.charAt(0);
    },
    loadNextGroup(allGroups) {
      if (this.currentGroupIndex >= allGroups.length) {
        uni.hideLoading();
        return;
      }
      const newGroup = allGroups[this.currentGroupIndex];
      this.groupedData.push(newGroup);
      this.currentGroupIndex++;
      
      // Use nextTick or setTimeout to allow UI update before next chunk
      setTimeout(() => {
        this.loadNextGroup(allGroups);
      }, 50); 
    },
    getFullGroupName(groupName, types) {
      for (let i = 0; i < types.length; i++) {
        if (types[i][0] === groupName) {
          return types[i].slice(1);
        }
      }
      return groupName; // Fallback
    },
    toggleGroup(index) {
      this.groupedData[index].expanded = !this.groupedData[index].expanded;
    },
    onPlaceTap(place) {
      this.selectedPlace = place;
      this.source = 'video';
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
    onPlaceTap2(group) {
        // The original code passed 'e' and got dataset.p (group object) and dataset.t (title)
        // Here we pass the group object directly.
        // Also original code constructed 'numberArray' from item.title.
        
        const numberArray = group.items.map(item => item.title);
        
        this.source = 'video';
        // Remove count from groupName if needed, but original used raw groupName from dataset which might be full string.
        // The stored filterItem is likely used for display.
        this.filterItem = group.groupName; 
        this.navIds = numberArray;
        
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
    toggleEx() {
      // Collapse all groups
      this.groupedData.forEach(group => {
        group.expanded = false;
      });
      // Scroll to top
      this.scrollTop = 0.1;
      this.$nextTick(() => {
          this.scrollTop = 0;
      });
    },
    // Helper methods equivalent to WXS
    getPrice(str) {
      if (str == 0) return "免费";
      if (String(str).slice(-1) == "元") return str;
      if (String(str).length > 3) return str;
      return '￥' + str + "元";
    },
    getTags(place) {
      if (!place.recomend) return [];
      return place.recomend.split('，');
    }
  }
}
</script>

<style scoped>
.container7 {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.group-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  padding: 10px 10px;
  margin: 8px 3px 0px;
  background-color: #fff;
  border-bottom: 0.5px solid #4b63ec;
  position: sticky;
  top: 0px;
  z-index: 100;
}

.group-items {
  display: flex;
  flex-wrap: wrap;
  padding-left: 8px;
  padding-right: 8px;
  padding-top: 8px;
}

.item {
  display: flex;
  width: 100%;
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 500;
}

.single {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 8px;
  margin-right: 8px;
  display: block;
  padding: 2px;
}

.place {
  position: relative;
  flex: 1;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  padding-bottom: 4px;
}

.place image {
  width: 100%;
  height: 100px;
}

.star-rating {
  display: flex;
  justify-content: center;
  padding: 3px;
  margin-bottom: 2px;
}

.star-rating image {
  width: 15px;
  height: 15px;
  margin: 0 2px;
}

.play-icon {
  position: absolute;
  top: 50px;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding-left: 2px;
  padding-bottom: 2px;
  color: white;
  font-size: 14px;
}

.play-icon::after {
  content: '▶';
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  margin-top: 1px;
  margin-left: 3px;
  margin-right: 3px;
  justify-content: center;
}

.tag {
  font-weight: 400;
  padding: 2px 3px;
  border-radius: 4px; 
  margin: 2px;
  border: 1px solid rgb(76, 145, 235);
  font-size: 10px;
  background-color: rgb(250, 250, 250);
}

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
