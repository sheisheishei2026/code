<template>
  <scroll-view :scroll-top="scrollTop" scroll-with-animation="true" :show-scrollbar="false" style="height:100vh;overflow: hidden;" scroll-y="true">
    <view class="container">
     
 
      <view class="scroll-wrapper">
        <text class="title title2" style="margin-top: 5px;">抢票公众号</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in offlist" :key="index">
            <view class="content-wrapper" @click="goToBooking(item.officalId)">
              <image mode="aspectFit" style="object-fit:contain;width:70px;height:70px" class="bgimg has-shadow" :src="'https://open.weixin.qq.com/qr/code?username=' + item.officalId" :show-menu-by-longpress="true" @click.stop="goToBooking2(item.officalId)" :lazy-load="true"></image>
              <text class="text-content" style="margin-top: 5px;">{{item.title}}</text>
              <text class="text-content2">{{item.go}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper" style="margin-top: 6px;">
        <text class="title title2" style="margin-top: 7px;">常用公众号</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in yuyue" :key="index">
            <view class="content-wrapper">
              <image mode="aspectFill" style="object-fit:contain;width:70px;height:70px" class="image-content has-shadow" :src="'https://open.weixin.qq.com/qr/code?username=' + item.officalId" :show-menu-by-longpress="true" :lazy-load="true"></image>
              <text class="text-content">{{item.name}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper" style="margin-top: 2px;margin-bottom: 5px;">
        <text class="title title2">抢票小程序</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in applist" :key="index">
            <view class="content-wrapper" @click="goToBooking(item.appId)">
              <image mode="aspectFill" class="bgimg has-shadow" :src="noNet ? '' : item.img" @click.stop="goToBooking2(item.appId)" :lazy-load="true"></image>
              <text class="text-content">{{item.title}}</text>
              <text class="text-content2">{{item.go}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper">
        <text class="title title2">玩乐小程序</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in youwan" :key="index">
            <view class="content-wrapper">
              <image mode="aspectFill" class="image-content has-shadow" :src="noNet ? '' : item.img" @click.stop="goToBooking2(item.officalId)" :lazy-load="true"></image>
              <text class="text-content">{{item.name}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper">
        <text class="title title2">常用小程序</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in apps" :key="index">
            <view class="content-wrapper">
              <image mode="aspectFill" class="image-content has-shadow" :src="noNet ? '' : item.img" @click.stop="goToBooking2(item.officalId)" :lazy-load="true"></image>
              <text class="text-content">{{item.name}}</text>
            </view>
          </view>
          <view class="scrollable-item">
            <view class="content-wrapper" @click="goToMindmap">
              <view class="mindmap-icon">
                <text class="mindmap-icon-text">🧠</text>
              </view>
              <text class="text-content">思维导图</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper">
        <text class="title title2">公园小程序</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in gongyuan" :key="index">
            <view class="content-wrapper">
              <image mode="aspectFill" class="image-content has-shadow" :src="noNet ? '' : item.img" @click.stop="goToBooking2(item.officalId)" :lazy-load="true"></image>
              <text class="text-content">{{item.name}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

      <view class="scroll-wrapper">
        <text class="title title2">北戴河小程序</text>
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in beidaihe" :key="index">
            <view class="content-wrapper">
              <image mode="aspectFill" class="image-content has-shadow" :src="noNet ? '' : item.img" @click.stop="goToBooking2(item.officalId)" :lazy-load="true"></image>
              <text class="text-content">{{item.name}}</text>
            </view>
          </view>
        </scroll-view>
      </view>

    </view>
  </scroll-view>
</template>

<script>
export default {
  data() {
    return {
      scrollTop: 0,
      tabClickTime: 0,
      noNet: false,
      applist: [
        { title: "科学技术馆", img: "https://img0.baidu.com/it/u=3595122741,1347595161&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500", appId: "wx2c0837274f1a69e1", go: "周六约下周六" },
        { title: "首都博物馆", img: "https://img2.baidu.com/it/u=2203903211,2500518477&fm=253&fmt=auto&app=138&f=JPEG?w=972&h=649", appId: "wx79ef8066f8c5edcd", go: "周六20:00约下周六" },
        { title: "国家博物馆", img: "https://img1.baidu.com/it/u=207479255,790142157&fm=253&fmt=auto&app=120&f=JPEG?w=939&h=500", appId: "wx9e2927dd595b0473", go: "周六17:00约下周六" },
        { title: "军事博物馆", img: "https://img2.baidu.com/it/u=3081116457,1929558041&fm=253&fmt=auto&app=138&f=JPEG?w=1200&h=800", appId: "wxe7ab4bac193578d0", go: "周六8,17,20约下周六" },
        { title: "考古博物馆", img: "https://img2.baidu.com/it/u=4183598114,2743717412&fm=253&fmt=auto&app=138&f=JPEG?w=1067&h=800", appId: "wx48b5cc9990544897", go: "周三9:00约周六" },
        { title: "电影博物馆", img: "https://img0.baidu.com/it/u=721523635,2118689203&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500", appId: "wx45289e4f06cf3058", go: "周二约周六" },
        { title: "天安门广场", img: "https://img1.baidu.com/it/u=1911777303,2142984604&fm=253&fmt=auto&app=120&f=JPEG?w=888&h=500", appId: "wx5387f2dc7079c2a7", go: "提前1天预约" },
        { title: "主席纪念堂", img: "https://i01piccdn.sogoucdn.com/52a6ec56bacb4ca0", appId: "wx492b5d2f5b89c11e", go: "周一12:30约周六" },
        { title: "人民大会堂", img: "https://img2.baidu.com/it/u=3199489220,364091175&fm=253&fmt=auto&app=138&f=JPEG?w=755&h=500", appId: "wxb2809a187df8351b", go: "周三17:00约周六" },
        { title: "故宫博物院", img: "https://img0.baidu.com/it/u=106280706,200541069&fm=253&fmt=auto&app=120&f=JPEG?w=844&h=500", appId: "wx13169e68a3e63e55", go: "周六20:00约下周六" },
        { title: "北京大学", img: "https://img1.baidu.com/it/u=1086753069,3888511882&fm=253&fmt=auto&app=120&f=JPEG?w=1422&h=800", appId: "wxae221464afae4826", go: "周六8:00约下周六" },
        { title: "清华大学", img: "https://img0.baidu.com/it/u=412491922,34672627&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500", appId: "wxef227a5869ad5e4a", go: "周六8:00约下周六" },
        { title: "北京电影学院", img: "https://img1.baidu.com/it/u=1233640341,1443501312&fm=253&fmt=auto&app=120&f=JPEG?w=1065&h=800", appId: "wxc9e3953cad2d0dcb", go: "周一约周六" },
        { title: "翠湖湿地公园", img: "https://img1.baidu.com/it/u=3532923378,1389796403&fm=253&fmt=auto&app=138&f=JPEG?w=772&h=500", appId: "wxefee5ed954ac8c32", go: "周六约下周六" }
      ],
      offlist: [
        { title: "北京科学中心", img: "https://img1.baidu.com/it/u=1056750251,722847045&fm=253&fmt=auto&app=120&f=JPEG?w=667&h=500", officalId: "gh_bc4e899b1071", go: "周三12:00约周六" },
        { title: "自然博物馆", img: "https://img0.baidu.com/it/u=299687060,2076407218&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500", officalId: "NNHMChina", go: "周三11:00约周六" },
        { title: "宣南博物馆", img: "https://img1.baidu.com/it/u=4034618618,2877704349&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500", officalId: "bjxnwhbwg", go: "周三9:00约周六" },
        { title: "北京公交馆", img: "https://img2.baidu.com/it/u=3479148926,1657722234&fm=253&fmt=auto&app=120&f=JPEG?w=800&h=500", officalId: "bjgjjt", go: "周六8:00约下周六" },
        { title: "天安门城楼", img: "https://img2.baidu.com/it/u=3072957233,2744214562&fm=253&fmt=auto&app=120&f=JPEG?w=785&h=409", officalId: "gh_35795f3720b7", go: "周六17:00约下周六" },
        { title: "正阳门箭楼", img: "https://img1.baidu.com/it/u=1850115789,3818410978&fm=253&fmt=auto?w=1026&h=664", officalId: "gh_776dc392acc0", go: "周六17:00约下周六" },
        { title: "西黄寺", img: "https://img2.baidu.com/it/u=4254709078,1531832146&fm=253&fmt=auto&app=138&f=JPEG?w=1067&h=800", officalId: "zyxgjfxy", go: "周六9:00约下周六" },
        { title: "礼品文物中心", img: "https://img0.baidu.com/it/u=413402926,3606447689&fm=253&fmt=auto&app=120&f=JPEG?w=1067&h=800", officalId: "gh_555e3bfe7458", go: "周一约周六" },
      ],
      apps: [
        { name: '有备旅行', officalId: 'sphdXGov8jfd0cu', img: "https://img2.baidu.com/it/u=4144865249,2413195635&fm=253&fmt=auto&app=120&f=JPEG?w=475&h=475" },
        { name: '听听北京之声', officalId: 'wx9e834ab051316085', img: "https://img1.baidu.com/it/u=2940821087,646991081&fm=253&fmt=auto&app=120&f=JPEG?w=759&h=506" },
        { name: '北京博物馆云', officalId: 'wxa56ef9f832fbb5a5', img: "https://img0.baidu.com/it/u=3412127417,3637841262&fm=253&fmt=auto&app=138&f=JPEG?w=839&h=500" },
        { name: '畅游公园', officalId: 'wxf0693a7822f75666', img: "https://img2.baidu.com/it/u=2278711874,1834532255&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500" },
        { name: '京体通', officalId: 'wx0923d61c2de2b800', img: "https://img1.baidu.com/it/u=3667785769,1474155931&fm=253&app=138&f=JPEG?w=800&h=1067" },
        { name: '户外活动', officalId: 'wx487e719e4761c259', img: "https://img1.baidu.com/it/u=844976206,3610703879&fm=253&fmt=auto&app=138&f=JPEG?w=873&h=500" },
        { name: '云上中轴', officalId: 'wxb1779061d6f3cf10', img: "https://img2.baidu.com/it/u=2134071793,917059668&fm=253&fmt=auto&app=120&f=JPEG?w=596&h=500" },
        { name: '携程', officalId: 'wx0e6ed4f51db9d078', img: "https://img2.baidu.com/it/u=134130241,1754648413&fm=253&fmt=auto&app=138&f=JPG?w=500&h=500" },
        { name: '马蜂窝', officalId: 'wx00b636a5ebdbcf7e', img: "https://img2.baidu.com/it/u=3535415330,2415146648&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500" },
        { name: '望京医院', officalId: 'wxaaaca52edc0a6e69', img: 'https://med-fe.cdn.bcebos.com/hospital/logo/101900.png?x-bce-process=image/auto-orient,o_1/resize,w_1242,limit_1/quality,q_85/format,f_auto' },
      ],
      youwan: [
        { name: '休闲玩乐', officalId: '222', img: "https://img2.baidu.com/it/u=2114916219,1102165437&fm=253&app=138&f=JPEG?w=500&h=500" },
        { name: '景点门票', officalId: '333', img: "https://img2.baidu.com/it/u=1375054299,2302461829&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500" },
        { name: '北京欢乐谷', officalId: 'wxe5f5c3aaeb99737f', img: "https://img1.baidu.com/it/u=2329821503,793178459&fm=253&fmt=auto&app=138&f=JPEG?w=760&h=451" },
        { name: '民族之夜', officalId: 'wxf9c1a1577d98d74f', img: "https://img0.baidu.com/it/u=3809370814,1478399080&fm=253&app=138&f=JPEG?w=667&h=500" },
        { name: '首都国际会展中心', officalId: 'wx7554a77851054d86', img: "https://img0.baidu.com/it/u=3566793678,4289761597&fm=253&fmt=auto&app=120&f=JPEG?w=1567&h=800" },
        { name: '798艺术区', officalId: 'wx8ac12e459f639ded', img: "https://img1.baidu.com/it/u=1844085115,2764763642&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500" },
        { name: '首钢园', officalId: 'wxdb31ab24e8691f16', img: "https://img2.baidu.com/it/u=2629528873,2136515087&fm=253&fmt=auto&app=120&f=JPEG?w=749&h=500" },
        { name: '古北水镇', officalId: 'wxc6c4e25e3fdfe943', img: "https://img2.baidu.com/it/u=4039291594,201093407&fm=253&fmt=auto&app=138&f=JPEG?w=760&h=760" },
        { name: '大运河文化旅游区', officalId: 'wxb4eb0a3e820af48b', img: "https://img2.baidu.com/it/u=2166958093,3180853304&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500" },
        { name: '八达岭旅游区', officalId: 'wx32de8a3dce14fe60', img: "https://img0.baidu.com/it/u=793699411,105712673&fm=253&app=138&f=JPEG?w=1067&h=800" },
        { name: '北京野生动物园', officalId: 'wx469f0f1493a75ded', img: "https://img0.baidu.com/it/u=3614488353,2814410164&fm=253&app=138&f=JPEG?w=667&h=500" },
        { name: '一键游房山', officalId: 'wx43c87e4ddbd64792', img: "https://img1.baidu.com/it/u=3869814993,3811695626&fm=253&fmt=auto&app=138&f=JPEG?w=1067&h=800" },
        { name: '环球影城度假区', officalId: 'wx3ba512d53df66a75', img: "https://img0.baidu.com/it/u=1561407606,3384185231&fm=253&fmt=auto&app=120&f=JPEG?w=667&h=500" },
        { name: '北戴河旅游', officalId: 'wxbb95e230c52e5003', img: "https://img2.baidu.com/it/u=1122094016,3259908981&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500" },
      ],
      gongyuan: [
        { name: '动物园', officalId: 'wx0e4127ba2ca59294', img: "https://img0.baidu.com/it/u=2253581115,1418322064&fm=253&fmt=auto&app=138&f=JPEG?w=754&h=500" },
        { name: '天坛公园', officalId: 'wx2c1510b12ab33d9b', img: "https://img1.baidu.com/it/u=582163622,3130046888&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500" },
        { name: '北海公园', officalId: 'wx753065d74bbdb095', img: "https://img2.baidu.com/it/u=2556784906,472119557&fm=253&fmt=auto&app=120&f=JPEG?w=1058&h=500" },
        { name: '颐和园', officalId: 'wx7a71ce8060ad1f41', img: "https://img1.baidu.com/it/u=501395727,2894665974&fm=253&app=138&f=JPEG?w=692&h=500" },
        { name: '大观园', officalId: 'wx08358fa2a60ea3a0', img: "https://img0.baidu.com/it/u=1054815838,942090789&fm=253&app=138&f=JPEG?w=546&h=392" },
        { name: '玉渊潭公园', officalId: 'wxa44e60ab68b38f70', img: "https://img0.baidu.com/it/u=3028908206,1218295609&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500" },
        { name: '紫竹院公园', officalId: 'wx86182298ccd131ce', img: "https://img0.baidu.com/it/u=2884818990,2474185653&fm=253&app=138&f=JPEG?w=667&h=500" },
        { name: '植物园', officalId: 'wxa2b6b82281a56ec9', img: "https://img0.baidu.com/it/u=3013469734,2704864831&fm=253&app=138&f=JPEG?w=692&h=500" },
        { name: '香山公园', officalId: 'wx3da8c9ad14ab137c', img: "https://img1.baidu.com/it/u=3402705995,938252593&fm=253&fmt=auto&app=138&f=JPEG?w=749&h=500" },
      ],
      beidaihe: [
        { name: '北戴河旅游', officalId: 'wxbb95e230c52e5003', img: "https://img2.baidu.com/it/u=1122094016,3259908981&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500" },
        { name: '碧螺塔', officalId: 'wx9ce93ee48dff2382', img: "https://img1.baidu.com/it/u=3724181079,3807975878&fm=253&fmt=auto&app=120&f=JPEG?w=667&h=500" },
        { name: '阿那亚', officalId: 'wx44f2f6e505bb3a75', img: "https://img2.baidu.com/it/u=358096395,2555945708&fm=253&fmt=auto&app=138&f=JPEG?w=759&h=427" },
        { name: '蔚蓝海岸', officalId: 'wx0e1e384bc5da7836', img: "https://img1.baidu.com/it/u=3487484842,979635316&fm=253&fmt=auto&app=138&f=JPEG?w=666&h=500" },
        { name: '老龙头景区', officalId: 'wxd8bd240f65ef594d', img: "https://img1.baidu.com/it/u=1441738843,130058793&fm=253&app=138&f=JPEG?w=677&h=500" },
        { name: '山海关古城', officalId: 'wxd8bd240f65ef594d', img: "https://img0.baidu.com/it/u=213105495,480451084&fm=253&fmt=auto&app=138&f=JPEG?w=1067&h=800" },
      ],
      yuyue: [
        { name: '党史展览馆', officalId: 'gh_72eb8e4c45ed' },
        { name: '国家会议中心', officalId: 'CNCC84372008' },
        { name: '工艺美术馆', officalId: 'gh_74f96355cf6b' },
        { name: '李大钊故居', officalId: 'gh_dd4ec92e97b5' },
        { name: '中国美术馆', officalId: 'namoc2016' },
        { name: '北京本地宝', officalId: 'bdbbeijing' },
        { name: '锦绣华北一卡通', officalId: 'tongpiaocn' },
        { name: '京津冀旅游年卡', officalId: 'jjjlyw' },
        { name: '北京庙会', officalId: 'beijingmiaohui' },
        { name: '文旅北京', officalId: 'gh_187a948dcbbc' },
      ],
    };
  },
  onShow() {
    const app = getApp();
    if (app && app.globalData) {
      this.noNet = app.globalData.noNet || false;
    }
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
    goToMen() {
      uni.navigateToMiniProgram({
        appId: "wxde8ac0a21135c07d",
        path: "ticket/pages/index/index",
        envVersion: 'release',
      });
    },
    goToPlay() {
      uni.navigateToMiniProgram({
        appId: "wxde8ac0a21135c07d",
        path: "play/pages/home/index",
        envVersion: 'release',
      });
    },
    goToBooking(appId) {
      if (appId && appId != '') {
        uni.navigateToMiniProgram({ appId: appId })
      }
    },
    goToBooking2(officalId) {
      if (officalId && officalId == '111') {
        uni.navigateTo({ url: '/pages/pics/pics' });
      } else if (officalId && officalId == 'sphdXGov8jfd0cu') {
        if (uni.openChannelsUserProfile) {
          uni.openChannelsUserProfile({ finderUserName: officalId })
        } else {
          uni.showToast({ title: '当前环境不支持', icon: 'none' });
        }
      } else if (officalId && officalId == '222') {
        this.goToPlay()
      } else if (officalId && officalId == '333') {
        this.goToMen()
      } else if (officalId && officalId != '') {
        uni.navigateToMiniProgram({ appId: officalId })
      }
    },
    navigateToVideoProfile() {
      if (uni.openChannelsUserProfile) {
         uni.openChannelsUserProfile({
          finderUserName: 'sph0zzQsso50ghm',
          success(res) {
            console.log('成功打开视频号主播资料页', res);
          },
          fail(err) {
            console.error('打开视频号主播资料页失败', err);
            uni.showToast({
              title: '打开失败，请稍后重试',
              icon: 'none'
            });
          }
        });
      }
    },
    onPlaceTap(place) {
      // Adapted to receive place directly or from event. original used e.currentTarget.dataset.place
      // If we use arguments in template, it is easier.
      const app = getApp();
      if(app && app.globalData) {
        app.globalData.selectedPlace = place;
      }
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
    goToMindmap() {
      uni.navigateTo({
        url: '/pages/mindmap/mindmap',
        success: function (res) {
          console.log('跳转到思维导图成功');
        },
        fail: function (err) {
          console.error('跳转失败:', err);
        }
      });
    },
    goToMagnifier() {
      uni.navigateTo({
        url: '/pages/magnifier/magnifier',
        success: function (res) {
          console.log('跳转到放大镜成功');
        },
        fail: function (err) {
          console.error('跳转失败:', err);
        }
      });
    },
    goToFlashlight() {
      uni.navigateTo({
        url: '/pages/flashlight/flashlight',
        success: function (res) {
          console.log('跳转到手电筒成功');
        },
        fail: function (err) {
          console.error('跳转失败:', err);
        }
      });
    },
  }
}
</script>

<style scoped>
.container {
  padding: 5px 0 5px 5px;
}

view::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.title {
  font-size: 15px;
  border-radius: 5px;
}

.title2 {
  padding: 0px 3px 0px 0px;
  writing-mode: vertical-rl;
  text-orientation: upright;
  align-self: start;
  margin-top: 6px;
  color: rgb(221, 132, 29);
}

.bgimg {
  width: 80px;
  height: 70px;
  object-fit: cover;
  border-radius: 5px;
}

.has-shadow {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.scroll-wrapper {
  width: 100%;
  height: 110px;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.scrollable-container {
  flex: 1;
  height: 128px;
  padding: 0px;
  overflow-x: auto;
  white-space: nowrap;
  margin-top: 10px;
  padding-bottom: 15px;
  margin-bottom: -15px;
}

.scrollable-item {
  width: 80px;
  display: inline-block;
  margin-right: 10px;
  background-color: white;
  border-radius: 12px;
  vertical-align: top;
  padding-left: 5px;
  padding-top: 5px;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0px;
}

.image-content {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 5px;
}

.text-content {
  font-size: 12px;
  font-weight: 500;
  color: #333;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  margin-top: 5px;
}

.text-content2 {
  font-size: 9px;
  color: #333;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  margin-top: 2px;
}

.mindmap-button-wrapper {
  padding: 15px;
  background-color: #ffffff;
  margin-bottom: 5px;
  display: flex;
  flex-direction: row;
  gap: 15px;
  justify-content: space-between;
}

.mindmap-button {
  flex: 1;
  min-width: 0;
  height: 70px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  flex-direction: column;
  padding: 8px 5px;
}

.mindmap-button:active {
  opacity: 0.9;
  transform: scale(0.98);
}

.mindmap-button-icon {
  font-size: 28px;
  margin-bottom: 6px;
}

.mindmap-button-text {
  font-size: 13px;
  font-weight: 600;
  color: #ffffff;
  white-space: nowrap;
}

.mindmap-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.mindmap-icon-text {
  font-size: 40px;
}
</style>
