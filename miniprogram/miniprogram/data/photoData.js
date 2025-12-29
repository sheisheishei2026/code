"use strict";
const photoData = [
  {
    id: 1,
    name: "宾要上房揭瓦",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101248/8c640f5f3b76cee752331042977da2b9/1040g00831c46pl620m005ngs092085odpg1cii0!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101248/dca2ff10db5eed80419f3d5723bd946e/1040g00830qkvq5ougc6g5ngs092085odq969a60!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101248/2d602af09eb8b35f45500a1fab350b54/1040g2sg30qslf3mpggkg5ngs092085oddk2n7t8!nc_n_webp_mw_1",
      "https://sns-avatar-qc.xhscdn.com/avatar/1040g2jo319u6sgr772605ngs092085odo63pljo?imageView2/2/w/540/format/webp|imageMogr2/strip2",
      "https://sns-webpic-qc.xhscdn.com/202512101312/21cda53f2e47ab4d2a2fca572786f3e3/1040g2sg311orh0rsgg5g5ngs092085odsu0e0a0!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101312/9597230275968edf1df6bdae1c05068d/1040g00830uhtqhb45a6g5ngs092085odo2db6tg!nc_n_webp_mw_1"
    ]
  },
  {
    id: 2,
    name: "刘钟德Randy",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101326/9381996adad6b1b2ec860360df34c266/1040g00831atqubg66m005p0js3ukc6nnnujh8po!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101327/f796e7ecace5de5fe4e617d95112949f/1040g2sg31a3uih6fmq705p0js3ukc6nnndk1918!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101327/17ae93df767447323a7c35b4f6e1ef5a/1040g2sg319g7n1bv6o705p0js3ukc6nnfgs4378!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101326/102fdfaebf8dd6741f9b00973502d5e9/1040g2sg31cku27qc08005p0js3ukc6nndmu0cqo!nc_n_webp_mw_1"
    ]
  },
  {
    id: 3,
    name: "文森特早点睡",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101328/5b32cbdb8a94ba27b2cdbd33fb6f6c2f/1040g2sg31pgma3rc2o7g5nne16p08ntgjbug328!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101328/1441496156c7211552984e66fb1c2270/1040g00831op4rbjnmg005nne16p08ntg7psd2j8!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101328/448aa2885500488a332c70c4d3dc6501/1040g00831o62r6676g005nne16p08ntgovr7fq8!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101328/4faf0f46df42c2d02dda8f803cca40bb/1040g2sg31n9leng1mme05nne16p08ntgfc5iabg!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101328/8e35c949cf8ad9e26fa7c5ebf9426d5e/1040g00831n10nb2jli005nne16p08ntggjb61hg!nc_n_webp_mw_1"
    ]
  },
  {
    id: 4,
    name: "Timeo",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101331/ede7c0a7667efbce0b4c49cb4b8e4bf4/1040g00831lsv11ae4s1048dv9jqgl4o70nsa340!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101331/6236ed25f9fc0d59900bf53f2a1cbf2f/1040g00831kfttr0u3q1g48dv9jqgl4o7b22qie8!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101331/70b65f82867254aa5ffc090f48fe112e/1040g00831gkb7o14j40g48dv9jqgl4o7ps833sg!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101331/4dbd9c6f2c0216f48631ee55db9dcf1c/1040g00831ffrhpidls0048dv9jqgl4o7j8a6rng!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101331/820d552cc281f3cd7cb281d93eed3bb6/1040g00831f8m4quq6m0048dv9jqgl4o7lodlup8!nc_n_webp_mw_1"
    ]
  },
  {
    id: 5,
    name: "Jacky.🐯",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101335/afa877478e776ea95ce830bc26326f4e/1040g2sg316q2p7be0q7040p72fjjdr962182mno!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101335/1b3bf58b90c4a002adf175fd19af53d2/1040g00831a15lniu6q0040p72fjjdr969m3kr78!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101335/9cf4450aef3b8fba9c462717a7dbfd96/notes_pre_post/1040g3k831ehafccth20040p72fjjdr96lsfnn7o!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101335/f6e65ce498711aff6687783920f24fba/notes_pre_post/1040g3k831iobbkb50ueg40p72fjjdr96rtqg5do!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101335/1f963392c113b8fca3752ef3596e32c0/1040g008316tqgheqgq0040p72fjjdr96fn0d1c8!nc_n_webp_mw_1"
    ]
  },
  {
    id: 6,
    name: "Lucas",
    images: [
      "https://sns-webpic-qc.xhscdn.com/202512101337/d78962d0283b991dc394dcb5292a134b/notes_pre_post/1040g3k831pfpuemvjc6g4161nsc3v1lb7cmlgio!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101337/ca4704addaead1e7ce7ed873c08ea9e7/1040g00831nnuir4cl8004161nsc3v1lb6oqrk9g!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101337/c6b54f6c7bd865c481a7bf8a9dbab9bf/notes_pre_post/1040g3k831mm6rlk5mm8g4161nsc3v1lb7viqr08!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101337/2ec642df2bb8ee7d37b894354e9ef15f/1040g00831lrnumotlu0g4161nsc3v1lbda33sn0!nc_n_webp_mw_1",
      "https://sns-webpic-qc.xhscdn.com/202512101337/35656cf13f4f986d0163c6fb4d7d355d/1040g00831o6dpv87lm004161nsc3v1lbjgbqj90!nc_n_webp_mw_1"
    ]
  }
];
exports.photoData = photoData;
//# sourceMappingURL=../../.sourcemap/mp-weixin/data/photoData.js.map
