<template>
  <view class="magnifier-container">
    <view class="header">
      <view class="header-title">放大镜</view>
    </view>
    
    <view class="content">
      <!-- 摄像头预览 -->
      <camera 
        id="magnifierCamera"
        device-position="back"
        :flash="flash"
        class="camera-view"
        :style="cameraStyle"
        @error="onCameraError"
        @initdone="onCameraInit"
      >
        <cover-view class="camera-overlay">
          <!-- 放大倍数显示 -->
          <cover-view class="zoom-info">
            {{ zoomLevel }}x
          </cover-view>
        </cover-view>
      </camera>
    </view>
    
    <!-- 底部控制栏 -->
    <view class="control-bar">
      <!-- 放大倍数控制 -->
      <view class="zoom-control">
        <text class="control-label">放大倍数</text>
        <slider 
          :value="zoomLevel" 
          min="1" 
          max="10" 
          step="0.5"
          @change="onZoomChange"
          activeColor="#4A90E2"
          backgroundColor="#e0e0e0"
          block-color="#4A90E2"
          block-size="20"
        />
        <text class="zoom-value">{{ zoomLevel }}x</text>
      </view>
      
      <!-- 功能按钮 -->
      <view class="function-buttons">
        <view class="func-btn" @click="toggleFlash">
          <text class="func-icon">{{ flash === 'on' ? '💡' : '🔦' }}</text>
          <text class="func-text">{{ flash === 'on' ? '关闭' : '开启' }}闪光灯</text>
        </view>
        <view class="func-btn" @click="resetZoom">
          <text class="func-icon">🔍</text>
          <text class="func-text">重置</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      zoomLevel: 5, // 默认5倍放大
      flash: 'off',
      cameraContext: null
    };
  },
  computed: {
    cameraStyle() {
      // 通过CSS transform实现视觉放大
      const scale = this.zoomLevel;
      return {
        transform: `scale(${scale})`,
        transformOrigin: 'center center',
        width: `${100 / scale}%`,
        height: `${100 / scale}%`,
        marginLeft: `${(scale - 1) * 50}%`,
        marginTop: `${(scale - 1) * 50}%`
      };
    }
  },
  onLoad() {
    this.initCamera();
  },
  onUnload() {
    this.stopCamera();
  },
  methods: {
    initCamera() {
      // 检查API是否支持
      if (typeof uni.createCameraContext !== 'function') {
        console.warn('createCameraContext 不支持，使用CSS放大');
        // 在H5环境下，camera组件可能也不支持，但CSS放大仍然可用
        return;
      }
      
      // 创建相机上下文
      try {
        this.cameraContext = uni.createCameraContext('magnifierCamera', this);
      } catch (e) {
        console.error('创建相机上下文失败:', e);
        // 即使创建失败，CSS放大仍然可用
      }
      
      // 尝试设置初始放大倍数（如果API支持）
      this.$nextTick(() => {
        setTimeout(() => {
          this.setZoom(this.zoomLevel);
        }, 1000);
      });
    },
    
    onCameraInit() {
      console.log('摄像头初始化完成');
      // 设置初始放大倍数
      setTimeout(() => {
        this.setZoom(this.zoomLevel);
      }, 500);
    },
    
    onCameraError(e) {
      console.error('摄像头错误:', e);
      uni.showModal({
        title: '提示',
        content: '摄像头打开失败，请检查相机权限',
        showCancel: false
      });
    },
    
    setZoom(zoom) {
      // 尝试使用API设置缩放（如果支持）
      try {
        if (this.cameraContext && this.cameraContext.setZoom) {
          this.cameraContext.setZoom({
            zoom: zoom,
            success: () => {
              console.log('设置放大倍数成功:', zoom);
            },
            fail: (err) => {
              console.log('API不支持缩放，使用CSS放大:', err);
              // 如果API不支持，使用CSS transform（已在computed中实现）
            }
          });
        } else {
          // 使用CSS transform（已在computed中实现）
          console.log('使用CSS放大:', zoom);
        }
      } catch (e) {
        console.log('设置放大倍数异常，使用CSS放大:', e);
        // 使用CSS transform（已在computed中实现）
      }
    },
    
    onZoomChange(e) {
      const newZoom = e.detail.value;
      this.zoomLevel = newZoom;
      this.setZoom(newZoom);
    },
    
    toggleFlash() {
      this.flash = this.flash === 'on' ? 'off' : 'on';
    },
    
    resetZoom() {
      this.zoomLevel = 5;
      this.setZoom(5);
      uni.showToast({
        title: '已重置为5倍',
        icon: 'success',
        duration: 1000
      });
    },
    
    stopCamera() {
      // 停止摄像头
      if (this.cameraContext) {
        this.cameraContext = null;
      }
    }
  }
};
</script>

<style scoped>
.magnifier-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #000000;
  overflow: hidden;
}

.header {
  height: 44px;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 100;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
}

.content {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #000000;
}

.camera-view {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.camera-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

.zoom-info {
  position: absolute;
  top: 60px;
  right: 20px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #ffffff;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
}

.control-bar {
  background-color: rgba(0, 0, 0, 0.8);
  padding: 15px;
  backdrop-filter: blur(10px);
}

.zoom-control {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.control-label {
  font-size: 14px;
  color: #ffffff;
  margin-right: 10px;
  min-width: 60px;
}

.zoom-control slider {
  flex: 1;
  margin: 0 10px;
}

.zoom-value {
  font-size: 16px;
  color: #4A90E2;
  font-weight: 600;
  min-width: 50px;
  text-align: right;
}

.function-buttons {
  display: flex;
  justify-content: space-around;
  gap: 10px;
}

.func-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s;
}

.func-btn:active {
  background-color: rgba(255, 255, 255, 0.2);
  transform: scale(0.95);
}

.func-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

.func-text {
  font-size: 12px;
  color: #ffffff;
}
</style>