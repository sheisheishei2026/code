Page({
  onLoad() {
    this.photoData = data_photoData.photoData;
    this.initWaterfall();
  },
 
    initWaterfall() {
      const leftColumn = [];
      const rightColumn = [];
      this.photoData.forEach((item, index) => {
        if (index % 2 === 0) {
          leftColumn.push(item);
        } else {
          rightColumn.push(item);
        }
      });
      this.columns[0] = leftColumn;
      this.columns[1] = rightColumn;
    },
    goToDetail(id) {
      common_vendor.index.navigateTo({
        url: `/pages/detail/detail?id=${id}`
      });
    }
  }
)