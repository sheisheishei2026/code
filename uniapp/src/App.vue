<script>
export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    console.log('App Show')
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style>
/*每个页面公共css */
.container {
  /* height: 100%; */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 200rpx 0;
  box-sizing: border-box;
} 

.back-to-top-btn {
  position: fixed;
  bottom: 10px;
  right: 10px;
  width: 35px;
  height: 35px;
  border-radius: 50px;
  background-color: rgb(255, 255, 255);
  /* border: 1px solid #bdbcbc;  */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  object-fit: contain;
  z-index: 1300;
}

.back-to-top-btn2 {
    position: fixed;
    bottom: 50px;
    right: 10px;
    width: 35px;
    height: 35px;
    border-radius: 50px;
    background-color: rgb(255, 255, 255);
    /* border: 1px solid #bdbcbc;  */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 定义 tabBar 按下效果 */
.tabbar-active {
  opacity: 0.7;
  transition: opacity 0.2s;
}

/* 应用按下效果到 tabBar 的图标和文字 */
page[style*="position: fixed; bottom: 0px; width: 100%;"] .wx-tabbar-item:active .wx-tabbar-icon,
page[style*="position: fixed; bottom: 0px; width: 100%;"] .wx-tabbar-item:active .wx-tabbar-label {
  opacity: 0.7;
}  

scroll-view ::-webkit-scrollbar {
    display: none;
    width: 0;
    height: 0;
    color: transparent;
}
</style>
