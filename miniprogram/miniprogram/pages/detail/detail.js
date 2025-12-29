Page({
  onLoad(options) {
    const id = options.id;
    this.getAlbumDetail(id);
  },
  onPageScroll(e) {
    const scrollTop = e.scrollTop;
    this.headerOpacity = Math.max(0.2, 1 - scrollTop / 200);
  },
    getAlbumDetail(id) {
      const album = data_photoData.photoData.find((item) => item.id == id);
      if (album) {
        this.albumInfo = album;
        this.initWaterfall();
      }
    },
    initWaterfall() {
      const leftColumn = [];
      const rightColumn = [];
      this.albumInfo.images.forEach((image, index) => {
        if (index % 2 === 0) {
          leftColumn.push(image);
        } else {
          rightColumn.push(image);
        }
      });
      this.columns[0] = leftColumn;
      this.columns[1] = rightColumn;
    },
    previewImage(url) {
      common_vendor.index.previewImage({
        urls: [url]
      });
    }
  }
)
