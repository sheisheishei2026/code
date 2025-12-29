Page({
  data: {
      images: [
        "https://upload-images.jianshu.io/upload_images/202169-1cbb224f8f811740.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-e6ce356c49e5eef5.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-47f2eb550db3cfaa.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-a57f25f53f04c4f7.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-30de3d95a866d1b9.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-e9fbea312f83ee81.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-28baf9251dd0c07c.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-61490c420623c99c.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-92fb3d836a33433b.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-d0c49c334666a46c.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-117dceb4f7e0be29.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-b7ff335874c2b200.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-33710879f62a8a8d.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-085404da524a05ef.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-1aefcd9cf275c134.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-0013e944f7e1301b.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-1596d05e08f00b92.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-207e51f1e79ecd01.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-8145657b2166ddc0.GIF?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-00730ab382e7d723.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-e6425168a2ada3e1.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-b7ecfd7adfd79839.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-42ee979cd25b8445.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-a40411b188909056.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-7f4dee293f3066c0.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-b249bca2b3c0f0ed.gif?imageMogr2/auto-orient/strip",
        "https://upload-images.jianshu.io/upload_images/202169-033098de3bc123f6.gif?imageMogr2/auto-orient/strip"
      ],
  },

  onLoad: function() {
  },

  previewImage: function(e) {
      const index = e.currentTarget.dataset.index;
      const urls = this.data.images[index]
      wx.previewImage({
          current: this.data.images[index],
          urls:  this.data.images
      });
  }
})
  