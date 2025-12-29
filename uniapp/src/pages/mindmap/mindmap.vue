<template>
  <view class="mindmap-container">
    <view class="toolbar">
      <view class="toolbar-item" @click="resetView">重置视图</view>
      <view class="toolbar-item" @click="saveMindmap">保存</view>
      <view class="toolbar-item" @click="loadMindmap">加载</view>
    </view>
    
    <view 
      class="mindmap-wrapper" 
      @touchstart="onTouchStart" 
      @touchmove="onTouchMove" 
      @touchend="onTouchEnd"
      :style="{ transform: `translate(${offsetX}px, ${offsetY}px)` }"
    >
      <!-- 根节点 -->
      <view 
        v-if="rootNode"
        class="node-wrapper"
        :style="getNodeStyle(rootNode)"
      >
        <view 
          class="node root-node"
          :class="{ 'node-selected': selectedNode && selectedNode.id === rootNode.id }"
          @click.stop="selectNode(rootNode)"
          @longpress="editNode(rootNode)"
        >
          <text class="node-text">{{ rootNode.text }}</text>
        </view>
        <view 
          class="action-button add-button"
          :style="getAddButtonStyle(rootNode)"
          @click.stop="addChildNode(rootNode)"
        >
          <text class="action-icon">+</text>
        </view>
        <view 
          v-if="rootNode.children && rootNode.children.length > 0"
          class="action-button delete-button"
          :style="getDeleteButtonStyle(rootNode)"
          @click.stop="deleteNode(rootNode)"
        >
          <text class="action-icon">−</text>
        </view>
      </view>
      
      <!-- 子节点 -->
      <template v-if="rootNode">
        <view 
          v-for="child in getAllChildren(rootNode)" 
          :key="child.id"
          class="node-wrapper"
          :style="getNodeStyle(child)"
        >
          <!-- 连线 -->
          <view 
            class="node-line"
            :style="getLineStyle(child)"
          ></view>
          
          <view 
            class="node child-node"
            :class="{ 'node-selected': selectedNode && selectedNode.id === child.id }"
            @click.stop="selectNode(child)"
            @longpress="editNode(child)"
          >
            <text class="node-text">{{ child.text }}</text>
          </view>
          <view 
            class="action-button add-button"
            :style="getAddButtonStyle(child)"
            @click.stop="addChildNode(child)"
          >
            <text class="action-icon">+</text>
          </view>
          <view 
            class="action-button delete-button"
            :style="getDeleteButtonStyle(child)"
            @click.stop="deleteNode(child)"
          >
            <text class="action-icon">−</text>
          </view>
        </view>
      </template>
    </view>
    
    <!-- 编辑节点弹窗 -->
    <view class="modal" v-if="showEditModal" @click.stop="closeEditModal">
      <view class="modal-content" @click.stop>
        <view class="modal-title">编辑节点</view>
        <input 
          class="modal-input" 
          v-model="editNodeText" 
          placeholder="输入节点文本"
          @confirm="confirmEdit"
        />
        <view class="modal-buttons">
          <view class="modal-btn" @click="confirmEdit">确定</view>
          <view class="modal-btn cancel" @click="closeEditModal">取消</view>
        </view>
      </view>
    </view>
    
    <!-- 添加节点弹窗 -->
    <view class="modal" v-if="showAddModal" @click.stop="closeAddModal">
      <view class="modal-content" @click.stop>
        <view class="modal-title">添加子节点</view>
        <input 
          class="modal-input" 
          v-model="newNodeText" 
          placeholder="输入节点文本"
          @confirm="confirmAdd"
        />
        <view class="modal-buttons">
          <view class="modal-btn" @click="confirmAdd">确定</view>
          <view class="modal-btn cancel" @click="closeAddModal">取消</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      rootNode: null,
      selectedNode: null,
      offsetX: 0,
      offsetY: 0,
      isDragging: false,
      lastTouchX: 0,
      lastTouchY: 0,
      showEditModal: false,
      showAddModal: false,
      editNodeText: '',
      newNodeText: '',
      editingNode: null,
      addingToNode: null,
      nodeIdCounter: 0,
      // 节点配置
      nodeConfig: {
        rootWidth: 140,
        rootHeight: 70,
        childWidth: 120,
        childHeight: 60,
        horizontalSpacing: 180,
        verticalSpacing: 100,
        addButtonSize: 32
      }
    };
  },
  onLoad() {
    this.initMindmap();
  },
  methods: {
    initMindmap() {
      // 获取屏幕尺寸
      const systemInfo = uni.getSystemInfoSync();
      const centerX = systemInfo.windowWidth / 2;
      const centerY = 150;
      
      // 初始化根节点
      this.rootNode = {
        id: this.nodeIdCounter++,
        text: '中心主题',
        x: centerX,
        y: centerY,
        width: this.nodeConfig.rootWidth,
        height: this.nodeConfig.rootHeight,
        children: [],
        parent: null,
        level: 0
      };
    },
    
    getAllChildren(node) {
      let allChildren = [];
      const traverse = (n) => {
        if (n.children) {
          n.children.forEach(child => {
            allChildren.push(child);
            traverse(child);
          });
        }
      };
      traverse(node);
      return allChildren;
    },
    
    getNodeStyle(node) {
      return {
        left: (node.x - node.width / 2) + 'px',
        top: (node.y - node.height / 2) + 'px',
        width: node.width + 'px',
        height: node.height + 'px'
      };
    },
    
    getAddButtonStyle(node) {
      // 加号按钮在节点右侧
      const buttonX = node.x + node.width / 2 + 12;
      const buttonY = node.y;
      return {
        left: (buttonX - this.nodeConfig.addButtonSize / 2) + 'px',
        top: (buttonY - this.nodeConfig.addButtonSize / 2) + 'px',
        width: this.nodeConfig.addButtonSize + 'px',
        height: this.nodeConfig.addButtonSize + 'px'
      };
    },
    
    getDeleteButtonStyle(node) {
      // 减号按钮在节点左侧
      const buttonX = node.x - node.width / 2 - 12;
      const buttonY = node.y;
      return {
        left: (buttonX - this.nodeConfig.addButtonSize / 2) + 'px',
        top: (buttonY - this.nodeConfig.addButtonSize / 2) + 'px',
        width: this.nodeConfig.addButtonSize + 'px',
        height: this.nodeConfig.addButtonSize + 'px'
      };
    },
    
    getLineStyle(childNode) {
      if (!childNode.parent) return {};
      
      const parent = childNode.parent;
      const parentX = parent.x;
      const parentY = parent.y + parent.height / 2;
      const childX = childNode.x;
      const childY = childNode.y - childNode.height / 2;
      
      // 计算连线的起点和终点
      const startX = parentX;
      const startY = parentY;
      const endX = childX;
      const endY = childY;
      
      // 计算连线的长度和角度
      const dx = endX - startX;
      const dy = endY - startY;
      const length = Math.sqrt(dx * dx + dy * dy);
      const angle = Math.atan2(dy, dx) * 180 / Math.PI;
      
      return {
        left: startX + 'px',
        top: startY + 'px',
        width: length + 'px',
        transform: `rotate(${angle}deg)`,
        transformOrigin: '0 0'
      };
    },
    
    selectNode(node) {
      this.selectedNode = node;
    },
    
    addChildNode(parentNode) {
      this.addingToNode = parentNode;
      this.newNodeText = '';
      this.showAddModal = true;
    },
    
    confirmAdd() {
      if (!this.addingToNode || !this.newNodeText.trim()) {
        this.closeAddModal();
        return;
      }
      
      const parent = this.addingToNode;
      const level = parent.level + 1;
      const childCount = parent.children.length;
      
      // 计算新节点的位置
      // 子节点在父节点下方，根据子节点数量水平分布
      const totalWidth = childCount * this.nodeConfig.horizontalSpacing;
      const startX = parent.x - totalWidth / 2;
      const newX = startX + childCount * this.nodeConfig.horizontalSpacing;
      const newY = parent.y + this.nodeConfig.verticalSpacing;
      
      const newNode = {
        id: this.nodeIdCounter++,
        text: this.newNodeText.trim(),
        x: newX,
        y: newY,
        width: this.nodeConfig.childWidth,
        height: this.nodeConfig.childHeight,
        children: [],
        parent: parent,
        level: level
      };
      
      parent.children.push(newNode);
      this.closeAddModal();
    },
    
    closeAddModal() {
      this.showAddModal = false;
      this.newNodeText = '';
      this.addingToNode = null;
    },
    
    editNode(node) {
      this.editingNode = node;
      this.editNodeText = node.text;
      this.showEditModal = true;
    },
    
    confirmEdit() {
      if (this.editingNode && this.editNodeText.trim()) {
        this.editingNode.text = this.editNodeText.trim();
      }
      this.closeEditModal();
    },
    
    closeEditModal() {
      this.showEditModal = false;
      this.editNodeText = '';
      this.editingNode = null;
    },
    
    deleteNode(node) {
      if (node === this.rootNode) {
        uni.showModal({
          title: '提示',
          content: '不能删除根节点，可以编辑其内容',
          showCancel: false
        });
        return;
      }
      
      uni.showModal({
        title: '确认删除',
        content: '确定要删除这个节点及其所有子节点吗？',
        success: (res) => {
          if (res.confirm) {
            const parent = node.parent;
            if (parent && parent.children) {
              const index = parent.children.indexOf(node);
              if (index > -1) {
                parent.children.splice(index, 1);
              }
            }
            if (this.selectedNode && this.selectedNode.id === node.id) {
              this.selectedNode = null;
            }
          }
        }
      });
    },
    
    onTouchStart(e) {
      if (e.touches.length === 1) {
        this.isDragging = true;
        this.lastTouchX = e.touches[0].clientX;
        this.lastTouchY = e.touches[0].clientY;
      }
    },
    
    onTouchMove(e) {
      if (this.isDragging && e.touches.length === 1) {
        const deltaX = e.touches[0].clientX - this.lastTouchX;
        const deltaY = e.touches[0].clientY - this.lastTouchY;
        
        this.offsetX += deltaX;
        this.offsetY += deltaY;
        
        this.lastTouchX = e.touches[0].clientX;
        this.lastTouchY = e.touches[0].clientY;
      }
    },
    
    onTouchEnd() {
      this.isDragging = false;
    },
    
    resetView() {
      this.offsetX = 0;
      this.offsetY = 0;
      this.selectedNode = null;
    },
    
    saveMindmap() {
      const mindmapData = JSON.stringify(this.rootNode);
      uni.setStorageSync('mindmapData', mindmapData);
      uni.showToast({ title: '保存成功', icon: 'success' });
    },
    
    loadMindmap() {
      const mindmapData = uni.getStorageSync('mindmapData');
      if (mindmapData) {
        try {
          this.rootNode = JSON.parse(mindmapData);
          this.updateNodeIdCounter(this.rootNode);
          this.selectedNode = null;
          this.resetView();
          uni.showToast({ title: '加载成功', icon: 'success' });
        } catch (e) {
          uni.showToast({ title: '加载失败', icon: 'none' });
        }
      } else {
        uni.showToast({ title: '没有保存的数据', icon: 'none' });
      }
    },
    
    updateNodeIdCounter(node) {
      if (node.id >= this.nodeIdCounter) {
        this.nodeIdCounter = node.id + 1;
      }
      if (node.children) {
        node.children.forEach(child => {
          this.updateNodeIdCounter(child);
        });
      }
    }
  }
};
</script>

<style scoped>
.mindmap-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  overflow: hidden;
}

.toolbar {
  display: flex;
  padding: 10px;
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  z-index: 100;
  flex-wrap: wrap;
}

.toolbar-item {
  padding: 8px 16px;
  margin: 4px;
  background-color: #4A90E2;
  color: #ffffff;
  border-radius: 4px;
  font-size: 12px;
  white-space: nowrap;
}

.toolbar-item:active {
  background-color: #357ABD;
}

.mindmap-wrapper {
  flex: 1;
  position: relative;
  width: 2000px;
  height: 2000px;
  touch-action: none;
  overflow: visible;
}

.node-wrapper {
  position: absolute;
  z-index: 10;
}

.node-line {
  position: absolute;
  height: 2px;
  background-color: #999999;
  z-index: 1;
  transform-origin: 0 0;
}

.node {
  position: relative;
  background-color: #ffffff;
  border: 2px solid #cccccc;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.2s;
}

.root-node {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  color: #ffffff;
  font-weight: 600;
}

.child-node {
  background-color: #ffffff;
  border-color: #cccccc;
  color: #333333;
}

.node-selected {
  border-color: #4A90E2;
  border-width: 3px;
  box-shadow: 0 4px 12px rgba(74, 144, 226, 0.3);
}

.node-text {
  font-size: 14px;
  text-align: center;
  padding: 8px;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.root-node .node-text {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
}

.action-button {
  position: absolute;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid #ffffff;
}

.add-button {
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
  box-shadow: 0 3px 10px rgba(74, 144, 226, 0.5);
}

.add-button:active {
  transform: scale(0.85);
  box-shadow: 0 2px 6px rgba(74, 144, 226, 0.4);
}

.delete-button {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  box-shadow: 0 3px 10px rgba(255, 107, 107, 0.5);
}

.delete-button:active {
  transform: scale(0.85);
  box-shadow: 0 2px 6px rgba(255, 107, 107, 0.4);
}

.action-icon {
  color: #ffffff;
  font-size: 24px;
  font-weight: bold;
  line-height: 1;
  user-select: none;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  width: 80%;
  max-width: 500px;
  background-color: #ffffff;
  border-radius: 8px;
  padding: 20px;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  text-align: center;
}

.modal-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 16px;
  box-sizing: border-box;
}

.modal-buttons {
  display: flex;
  justify-content: space-around;
}

.modal-btn {
  flex: 1;
  padding: 10px;
  margin: 0 8px;
  background-color: #4A90E2;
  color: #ffffff;
  text-align: center;
  border-radius: 4px;
  font-size: 14px;
}

.modal-btn.cancel {
  background-color: #999999;
}

.modal-btn:active {
  opacity: 0.8;
}
</style>