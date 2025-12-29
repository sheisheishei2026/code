<template>
  <view class="flashlight-container">
    <view class="header">
      <view class="header-title">手电筒</view>
    </view>
    
    <view class="content">
      <!-- 隐藏的摄像头用于控制闪光灯 -->
      <camera 
        id="flashlightCamera"
        device-position="back"
        :flash="flash"
        class="hidden-camera"
        @error="onCameraError"
        @initdone="onCameraInit"
      ></camera>
      
      <!-- 手电筒图标区域 -->
      <view class="flashlight-icon-wrapper">
        <view 
          class="flashlight-icon" 
          :class="{ 'flashlight-on': isOn }"
          @click="toggleFlashlight"
        >
          <text class="icon-text">🔦</text>
        </view>
        <text class="status-text">{{ isOn ? '已开启' : '已关闭' }}</text>
      </view>
      
      <!-- 控制按钮 -->
      <view class="control-buttons">
        <view 
          class="control-btn" 
          :class="{ 'btn-active': isOn }"
          @click="toggleFlashlight"
        >
          <text class="btn-icon">{{ isOn ? '💡' : '🔦' }}</text>
          <text class="btn-text">{{ isOn ? '关闭' : '开启' }}</text>
        </view>
      </view>
      
      <!-- 亮度控制 -->
      <view class="brightness-control" v-if="isOn">
        <text class="control-label">亮度调节</text>
        <slider 
          :value="brightness" 
          min="0" 
          max="100" 
          step="10"
          @change="onBrightnessChange"
          activeColor="#FFD700"
          backgroundColor="#e0e0e0"
          block-color="#FFD700"
          block-size="20"
        />
        <text class="brightness-value">{{ brightness }}%</text>
      </view>
      
      <!-- 闪烁模式 -->
      <view class="flash-mode" v-if="isOn">
        <text class="mode-label">闪烁模式</text>
        <view class="mode-buttons">
          <view 
            class="mode-btn" 
            :class="{ 'mode-active': flashMode === 'normal' }"
            @click="setFlashMode('normal')"
          >
            常亮
          </view>
          <view 
            class="mode-btn" 
            :class="{ 'mode-active': flashMode === 'slow' }"
            @click="setFlashMode('slow')"
          >
            慢闪
          </view>
          <view 
            class="mode-btn" 
            :class="{ 'mode-active': flashMode === 'fast' }"
            @click="setFlashMode('fast')"
          >
            快闪
          </view>
        </view>
      </view>
    </view>
    
    <!-- 底部提示 -->
    <view class="footer-tip">
      <text class="tip-text">💡 提示：手电筒功能需要相机权限</text>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      isOn: false,
      brightness: 100,
      flashMode: 'normal', // normal, slow, fast
      flashTimer: null,
      cameraContext: null,
      flash: 'off'
    };
  },
  onLoad() {
    // 页面加载时自动打开手电筒
    this.initCamera();
  },
  onUnload() {
    // 页面卸载时关闭手电筒
    this.turnOffFlashlight();
  },
  onHide() {
    // 页面隐藏时关闭手电筒
    this.turnOffFlashlight();
  },
  methods: {
    initCamera() {
      // 检查API是否支持
      if (typeof uni.createCameraContext !== 'function') {
        console.warn('createCameraContext 不支持');
        uni.showModal({
          title: '提示',
          content: '手电筒功能在当前环境下不可用，请使用手机浏览器或小程序打开',
          showCancel: false
        });
        return;
      }
      
      // 创建相机上下文
      try {
        this.cameraContext = uni.createCameraContext('flashlightCamera', this);
      } catch (e) {
        console.error('创建相机上下文失败:', e);
        uni.showModal({
          title: '提示',
          content: '无法初始化相机，请检查相机权限',
          showCancel: false
        });
        return;
      }
      
      // 自动打开手电筒
      this.$nextTick(() => {
        setTimeout(() => {
          this.turnOnFlashlight();
        }, 500);
      });
    },
    
    onCameraInit() {
      console.log('摄像头初始化完成');
      // 自动打开手电筒
      setTimeout(() => {
        this.turnOnFlashlight();
      }, 300);
    },
    
    onCameraError(e) {
      console.error('摄像头错误:', e);
      uni.showModal({
        title: '提示',
        content: '摄像头打开失败，请检查相机权限',
        showCancel: false
      });
    },
    
    toggleFlashlight() {
      if (this.isOn) {
        this.turnOffFlashlight();
      } else {
        this.turnOnFlashlight();
      }
    },
    
    turnOnFlashlight() {
      // 通过camera组件的flash属性控制闪光灯
      this.flash = 'on';
      this.isOn = true;
      this.startFlashMode();
      
      // 尝试使用API（如果支持）
      if (this.cameraContext && typeof uni.setFlashlightState === 'function') {
        try {
          uni.setFlashlightState({
            state: true,
            success: () => {
              console.log('手电筒已开启');
            },
            fail: (err) => {
              console.log('使用camera组件控制闪光灯:', err);
            }
          });
        } catch (e) {
          console.log('使用camera组件控制闪光灯:', e);
        }
      }
    },
    
    turnOffFlashlight() {
      this.flash = 'off';
      this.isOn = false;
      this.stopFlashMode();
      
      // 尝试使用API关闭（如果支持）
      if (this.cameraContext && typeof uni.setFlashlightState === 'function') {
        try {
          uni.setFlashlightState({
            state: false,
            success: () => {
              console.log('手电筒已关闭');
            },
            fail: (err) => {
              console.log('使用camera组件控制闪光灯:', err);
            }
          });
        } catch (e) {
          console.log('使用camera组件控制闪光灯:', e);
        }
      }
    },
    
    onBrightnessChange(e) {
      this.brightness = e.detail.value;
      // 注意：实际亮度调节可能需要平台特定 API
      // 这里主要是 UI 展示
    },
    
    setFlashMode(mode) {
      this.flashMode = mode;
      this.stopFlashMode();
      if (this.isOn) {
        this.startFlashMode();
      }
    },
    
    startFlashMode() {
      this.stopFlashMode();
      
      if (this.flashMode === 'normal') {
        // 常亮模式，保持开启
        this.flash = 'on';
        return;
      }
      
      let interval = 0;
      if (this.flashMode === 'slow') {
        interval = 1000; // 慢闪：1秒
      } else if (this.flashMode === 'fast') {
        interval = 200; // 快闪：0.2秒
      }
      
      if (interval > 0) {
        let flashState = true;
        this.flashTimer = setInterval(() => {
          flashState = !flashState;
          this.flash = flashState ? 'on' : 'off';
          
          // 如果API支持，也调用API
          if (typeof uni.setFlashlightState === 'function') {
            try {
              uni.setFlashlightState({
                state: flashState,
                success: () => {
                  // 闪烁成功
                },
                fail: () => {
                  // 闪烁失败，停止定时器
                  this.stopFlashMode();
                }
              });
            } catch (e) {
              console.log('闪烁控制失败:', e);
            }
          }
        }, interval);
      }
    },
    
    stopFlashMode() {
      if (this.flashTimer) {
        clearInterval(this.flashTimer);
        this.flashTimer = null;
      }
    }
  }
};
</script>

<style scoped>
.flashlight-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.header {
  height: 44px;
  background-color: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
}

.hidden-camera {
  position: absolute;
  width: 1px;
  height: 1px;
  opacity: 0;
  pointer-events: none;
  z-index: -1;
}

.flashlight-icon-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60px;
}

.flashlight-icon {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  transition: all 0.3s;
  margin-bottom: 20px;
}

.flashlight-icon.flashlight-on {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  box-shadow: 0 0 60px rgba(255, 215, 0, 0.8), 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

.icon-text {
  font-size: 100px;
}

.status-text {
  font-size: 24px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.control-buttons {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}

.control-btn {
  width: 200px;
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.control-btn.btn-active {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  box-shadow: 0 4px 20px rgba(255, 215, 0, 0.6);
}

.control-btn:active {
  transform: scale(0.95);
}

.btn-icon {
  font-size: 28px;
  margin-right: 10px;
}

.btn-text {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.brightness-control {
  width: 100%;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.control-label {
  font-size: 14px;
  color: #ffffff;
  margin-bottom: 10px;
  display: block;
}

.brightness-control slider {
  margin: 10px 0;
}

.brightness-value {
  font-size: 16px;
  color: #FFD700;
  font-weight: 600;
  text-align: right;
  display: block;
  margin-top: 10px;
}

.flash-mode {
  width: 100%;
  padding: 20px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  backdrop-filter: blur(10px);
}

.mode-label {
  font-size: 14px;
  color: #ffffff;
  margin-bottom: 15px;
  display: block;
}

.mode-buttons {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.mode-btn {
  flex: 1;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  text-align: center;
  color: #ffffff;
  font-size: 14px;
  transition: all 0.3s;
}

.mode-btn.mode-active {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #000000;
  font-weight: 600;
  box-shadow: 0 2px 10px rgba(255, 215, 0, 0.4);
}

.mode-btn:active {
  transform: scale(0.95);
}

.footer-tip {
  padding: 15px;
  background-color: rgba(0, 0, 0, 0.3);
  text-align: center;
}

.tip-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}
</style>