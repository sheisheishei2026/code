<template>
  <scroll-view 
    id="scroll" 
    :show-scrollbar="false" 
    :scroll-top="scrollTop" 
    :refresher-enabled="true"
    @refresherrefresh="onRefresherRefresh" 
    :refresher-triggered="refresherTriggered"
    scroll-with-animation="true" 
    style="height: 100vh; overflow: hidden;" 
    scroll-y="true"
  >
    <view class="container" v-for="(item2, index1) in displayedList" :key="index1">
      <text class="title" :style="{ backgroundColor: getTitleColor(item2) }" @click="goToMap(item2)">{{item2.title}}</text>
      <text class="desc" v-if="item2.desc" @click="goToMap(item2)">{{getDesc(item2)}}</text>
      <view class="scroll-wrapper">
        <scroll-view class="scrollable-container" scroll-x="true" scroll-left="0" enable-flex="true">
          <view class="scrollable-item" v-for="(item, index) in item2.navList" :key="index">
            <view class="content-wrapper" @click="onPlaceTap(item, item2.navList)">
              <image mode="aspectFill" class="image-content has-shadow" :src="noNet ? '' : item.img" @load="onImageLoad($event, index, index1)" @error="onImageError($event, index, index1)" :lazy-load="true"></image>
              <text class="text-content">{{item.title}}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </scroll-view>
</template>

<script>
import { mapState, mapWritableState } from 'pinia';
import { useGlobalStore } from '@/stores/global';

export default {
  data() {
    return {
      scrollTop: 0,
      refresherTriggered: false,
      tabClickTime: 0,
      noNet: false,
      displayedList: [],
      all: [
        {
          title: "值得去的景点",
          nav: ["国家植物园", "西山森林公园", "SKP", "SKP-S", "紫檀博物馆", "御仙都皇家菜博物馆", "颐和园","奥北森林公园二期北园","红星胡同", "华熙Live", "古北水镇",  "银泰百货(故宫风)", "君悦大酒店", "赵府街副食店", "1号线时光列车", "北京欢乐谷", "越界锦荟园", "亚洲金融大厦(国图书店)", "明城墙遗址公园","中国尊", "中央广播电视总台","天安门城楼", "人民大会堂", "天坛公园", "鼓楼", "西什库教堂","中央美术学院美术馆","首钢大桥","首钢园","广阳书院","明十三陵","国际鲜花港","七孔桥花海", "友谊宾馆",
            "海淀公园", "花乡奥莱", "蓝调薰衣草庄园", "模式口文化街", "牛栏山酒厂文化苑", "紫玉山庄", "白云观", "妙音白塔寺","北宫森林公园", "中国油画院", "鸟巢", "世界公园", "健锐营演武厅", "故宫博物院","中央美术学院美术馆",
            "石景山规划展览馆","民航博物馆","电影博物馆","铁道博物馆(东郊馆)","草场地艺术区红砖","北投奥园1314","中关村展示中心","石景山游乐园"],
          navList: [],
          ifs: "一起去"
        },
        {
          title: "拍照绝美的地方",
          nav: ["天坛公园", "国家大剧院", "凤凰中心", "红砖美术馆", "美克洞学馆", "故宫博物院", "大观园", "颐和园", "鼓楼", "西黄寺", "世界公园", "西什库教堂", "王府井天主教堂", "陶然亭公园", "人定湖公园", "太庙", "中国油画院", "盛世南宫影城", "华侨历史博物馆", "罗红摄影艺术馆", "钓鱼台银杏大道", "前门大街", "荷花市场", "南环大桥", "首钢大桥", "丘比特庄园"],
          navList: [],
          ifs: "拍照绝美"
        },
        {
          title: "看表演",
          nav: ["宣南文化博物馆", "御仙都皇家菜博物馆", "李大钊故居", "北京欢乐谷", "鼓楼", "盛世京城大剧院", "太平洋海底世界", "北京海洋馆", "富国海底世界", "环球影城", "民族之夜"],
          navList: [],
          ifs: "看表演"
        },
        {
          title: "游乐园",
          nav: ["北京欢乐谷", "石景山游乐园", "南宫五洲植物乐园", "环球影城", "泡泡玛特城市乐园", "朝阳公园", "水立方嬉水乐园", "阳光拍拍熊列车主题乐园"],
          navList: [],
          ifs: "游乐园"
        },
        {
          title: "夏日玩水",
          nav: ["亮马河", "玛雅海滩水乐园", "水立方嬉水乐园", "龙脉熔岩温泉", "南宫温泉水世界", "欢乐水魔方", "温都水城", "什刹海体育广场", "奥林匹克水上公园", "南苑森林湿地公园"],
          navList: [],
          ifs: "夏日玩水"
        },
        {
          title: "秋日银杏",
          nav: ["阜成路银杏大道桥", "常营公园", "什坊小街银杏大道","法源寺", "韩美林艺术馆", "奥林匹克公园", "友谊宾馆", "钓鱼台银杏大道", "园博园", "香山公园", "南彩银杏园", "石刻艺术博物馆","朝阳公园", "张各庄银杏林", "玲珑塔公园","北京大学", "圆明园", "元大都城垣遗址公园","奥林匹克森林公园", "八大处公园", "清华大学艺术博物馆", "天坛公园", "东交民巷", "中山公园","地坛公园", "大觉寺", "红螺寺", "平房公园","东坝郊野公园"],
          navList: [],
          ifs: "秋日银杏"
        },
        {
          title: "粉黛花海",
          nav: ["奥北森林公园二期北园", "奥林匹克森林公园", "官庄公园","温榆河公园"],
          navList: [],
          ifs: "粉黛花海"
        },
        {
          title: "冬日人少",
          nav: ["天坛公园", "故宫博物院", "国家大剧院","八达岭长城","颐和园", "恭王府"],
          navList: [],
          ifs: "冬日人少"
        },
        {
          title: "去看大海，晒太阳",
          nav: ["碧螺塔","鸽子窝公园","秦皇求仙入海处","平水桥浴场","东二路浴场", "阿那亚", "南戴河旅游区", "欢乐湾", "老龙头景区", "蔚蓝海岸"],
          navList: [],
          ifs: "沙滩大海"
        },
        {
          title: "洗浴泡温泉",
          nav: ["龙脉熔岩温泉", "九华山庄", "水裹汤泉", "蟹岛度假村", "蓝调薰衣草庄园", "南宫温泉水世界", "塞纳河商务会馆"],
          navList: [],
          ifs: "洗浴温泉"
        },
        {
          title: "看巨幕，球幕，天幕，地幕",
          nav: ["党史展览馆", "北京科学中心", "电影博物馆", "科学技术馆", "科学家博物馆", "北京天文馆", "世贸天阶", "考古博物馆"],
          navList: [],
          ifs: "看巨幕"
        },
        {
          title: "互动畅玩的博物馆",
          nav: ["奥运博物馆", "科学技术馆","汽车博物馆", "消防博物馆","北京科学中心", "北京公交馆", "北京天文馆", "军事博物馆"],
          navList: [],
          ifs: "互动畅玩"
        },
        {
          title: "去露营",
          nav: ["海淀公园", "朝阳公园", "奥林匹克森林公园", "园博园"],
          navList: [],
          ifs: "去露营"
        },
        {
          title: "美容按摩",
          nav: ["丽都美容", "望京SOHO", "国贸商场"],
          navList: [],
          ifs: "美容按摩"
        },
        {
          title: "酒吧社交",
          nav: ["目的地酒吧", "PoolBar(桌上足球)", "银泰帝都秀酒吧"],
          navList: [],
          ifs: "酒吧社交"
        },
        {
          title: "去运动，网球，羽毛球，篮球，足球，乒乓球",
          nav: ["国家速滑馆", "钻石球场", "首都体育学院", "回龙观文化体育中心", "望京体育公园", "东四体育文化中心",],
          navList: [],
          ifs: "去运动"
        },
        {
          title: "健身锻炼游泳",
          nav: ["望京SOHO", "银河SOHO", "北京体育大学", "首都体育学院", "健身房", "鼎成健身房", "CrossfitDAA", "望京体育公园", "太极园区"],
          navList: [],
          ifs: "健身锻炼游泳"
        },
        {
          title: "听音乐会",
          nav: ["朝阳公园", "北投购物公园", "北小河公园", "奥林匹克森林公园", "海淀公园", "国家大剧院", "鸟巢", "北京音乐产业园"],
          navList: [],
          ifs: "音乐会"
        },
        {
          title: "周末常去",
          nav: ["北京欢乐谷", "亮马河", "首都图书馆", "北京体育大学", "健身房", "鼎成健身房", "民族之夜", "北京科学中心", "电影博物馆", "党史展览馆", "恭王府", "水立方嬉水乐园", "望京8号轰趴馆"],
          navList: [],
          ifs: "周末常去"
        },
        {
          title: "看专题会展",
          nav: ["国家会议中心", "中关村国际创新中心", "北京展览馆", "首都国际会展中心", "首钢国际会展中心", "农业展览馆", "酷车小镇"],
          navList: [],
          ifs: "会展中心"
        },
        {
          title: "看话剧，演出，欣赏艺术",
          nav: ["国家大剧院", "北京艺术中心", "北京人艺戏剧博物馆", "华侨城大剧院", "老舍茶馆", "台湖舞美艺术博物馆", "木偶艺术剧院博物馆"],
          navList: [],
          ifs: "剧场"
        },
        {
          title: "去读书",
          nav: ["首都图书馆", "国家图书馆", "北京城市图书馆", "钟书阁(西单老佛爷)", "钟书阁(海淀融科)", "西单图书大厦", "模范书局", "正阳书局", "泰舍书局", "角楼图书馆", "涵芬楼书店", "王府井书店", "北京图书大厦", "林白水纪念馆", "PageOne", "亚洲金融大厦(国图书店)", "红楼公共藏书馆", "茑屋书店"],
          navList: [],
          ifs: "去读书"
        },
        {
          title: "去骑行",
          nav: ["长安街骑行", "什刹海体育广场", "清河滨水慢行系统","清河之洲", "奥林匹克公园", "大运河文化旅游区", "龙潭公园", "北坞公园", "张自忠路"],
          navList: [],
          ifs: "骑行路线"
        },
        {
          title: "户外晒太阳",
          nav: ["大望京公园", "望和公园北园", "北小河公园", "昌平滨河森林公园", "勇士营郊野公园", "朝阳公园", "太阳宫公园", "四得公园", "望湖公园", "太阳宫公园"],
          navList: [],
          ifs: "晒太阳"
        },
        {
          title: "看路人",
          nav: ["北京体育大学", "亮马河", "南苑森林湿地公园", "水立方嬉水乐园","什刹海体育广场","路县故城遗址博物馆", "北京电影学院","国家会议中心","中医药大学浴室"],
          navList: [],
          ifs: "看路人"
        },
        {
          title: "看攀岩",
          nav: ["北投奥园1314", "日坛公园", "回龙观文化体育中心"],
          navList: [],
          ifs: "看攀岩"
        },
        {
          title: "玩VR",
          nav: ["科学技术馆","石景山科技馆", "京东MALL(海户屯)", "京东MALL(九龙山)"],
          navList: [],
          ifs: "玩VR"
        },
        {
          title: "电玩游戏",
          nav: ["望京8号轰趴馆","毕淘买生活广场","合生麒麟社","索尼体验店", "京东MALL(海户屯)", "京东MALL(九龙山)"],
          navList: [],
          ifs: "玩VR"
        },
        {
          title: "5A景区",
          nav: ["故宫博物院", "天坛公园", "颐和园", "圆明园", "恭王府", "明十三陵", "慕田峪长城", "八达岭长城", "大运河文化旅游区", "奥林匹克公园"],
          navList: [],
          ifs: "5A景区"
        },
        {
          title: "动物乐园",
          nav: ["动物园", "北京野生动物园", "八达岭野生动物园", "太平洋海底世界", "北京海洋馆", "富国海底世界", "南宫五洲植物乐园", "紫玉山庄", "南海子公园", "动物博物馆", "自然博物馆", "古动物馆", "中国地质博物馆", "地质大学博物馆"],
          navList: [],
          ifs: "动物乐园"
        },
        {
          title: "最美地铁",
          nav: ["西郊线旅游小火车", "S2线观赏花海", "1号线时光列车","19号线商务快线", "3号线时空隧道","S1磁悬浮地铁"],
          navList: [],
          ifs: "最美地铁"
        },
        {
          title: "欣赏雕塑艺术",
          nav: ["798艺术区", "世界公园", "卢沟桥文化旅游区", "北京国际雕塑公园", "人定湖公园", "仰山公园", "金台艺术馆", "宋庄艺术区", "韩美林艺术馆", "英杰硬石博物馆"],
          navList: [],
          ifs: "雕塑艺术"
        },
        {
          title: "看飞机大炮，火车轮船，导弹坦克，机枪武器",
          nav: ["军事博物馆", "北京公交馆", "798艺术区", "北京大运河博物馆", "铁道博物馆(东郊馆)", "民航博物馆", "航天博物馆", "海关博物馆", "汽车博物馆", "北京航空航天博物馆", "京张铁路遗址公园南段", "创1958园区", "南苑森林湿地公园"],
          navList: [],
          ifs: "车船机炮"
        },
        {
          title: "夜景美丽",
          nav: ["鸟巢", "水立方嬉水乐园", "奥林匹克塔", "鼓楼", "后海银锭桥", "北京欢乐谷", "御仙都皇家菜博物馆", "华熙Live", "世贸天阶", "蓝色港湾", "亮马河", "环球影城", "望京SOHO", "银河SOHO", "司马台长城", "慕田峪长城", "民族之夜", "长安街", "高碑店漕运夜景", "泡泡玛特城市乐园"],
          navList: [],
          ifs: "看夜景"
        },
        {
          title: "废弃氛围",
          nav: ["五只猫", "欧洲小镇", "通惠国际文化创意大街", "乐多港奇幻乐园", "BDMG涂鸦公园", "创1958园区"],
          navList: [],
          ifs: "废弃氛围"
        },
        {
          title: "鲜花丛林植物园",
          nav: ["国家植物园", "北京国际鲜花港", "世界花卉大观园", "七孔桥花海", "蓝调薰衣草庄园", "南宫五洲植物乐园"],
          navList: [],
          ifs: "鲜花丛林"
        },
        {
          title: "欣赏园林建筑艺术",
          nav: ["世界公园", "北海公园", "颐和园", "中华民族园", "园林博物馆", "园博园", "友谊宾馆", "白云观",],
          navList: [],
          ifs: "园林建筑"
        },
        {
          title: "特色饭店",
          nav: ["御仙都皇家菜博物馆", "郡王府饭店", "京能酒店和风景里", "白家大院"],
          navList: [],
          ifs: "特色饭店"
        },
        {
          title: "摸城墙",
          nav: ["正阳门箭楼", "健锐营演武厅", "明城墙遗址公园", "古观象台", "天安门城楼", "卢沟桥文化旅游区", "八达岭长城", "德胜门箭楼", "永定门公园", "西便门城墙公园", "司马台长城"],
          navList: [],
          ifs: "看城墙"
        },
        {
          title: "值得拍照的大厦",
          nav: ["城奥大厦", "中关村SOHO", "银河SOHO", "中国技术交易大厦", "保利艺术博物馆", "亚洲金融大厦(国图书店)", "百度大厦", "骏豪大厦"],
          navList: [],
          ifs: "拍大厦"
        },
        {
          title: "艺术馆看画展",
          nav: ["中央美术学院美术馆", "中国工艺美术馆", "中国美术馆", "韩美林艺术馆", "中国油画院", "国家画院美术馆", "北京画院美术馆", "壹美美术馆", "侨福芳草地艺术中心", "东岳美术馆", "红砖美术馆", "宋庄美术馆", "松美术馆", "西海美术馆", "启皓艺术馆", "时空集美术馆", "槐轩", "徐悲鸿纪念馆", "爱慕美术馆", "老甲艺术馆", "杏坛美术馆", "民生现代美术馆", "X美术馆(郎园)"],
          navList: [],
          ifs: "逛艺术馆"
        },
        {
          title: "北京城建都考古历史专题展馆",
          nav: ["北京市档案馆", "首都博物馆", "第一历史档案馆", "北京市方志馆", "故宫博物院", "西周燕都遗址博物馆", "金中都公园(建都之始)", "金中都水关遗址", "周口店北京人遗址博物馆", "大葆台汉墓博物馆"],
          navList: [],
          ifs: "北京历史"
        },
        {
          title: "有山有水有长城",
          nav: ["八达岭长城", "响水湖长城", "黄花城水长城", "司马台长城", "慕田峪长城", "居庸关长城", "‌水关长城"],
          navList: [],
          ifs: "爬长城"
        },
        {
          title: "拍教堂",
          nav: ["西什库教堂", "王府井天主教堂", "宣武门天主教堂", "西直门教堂", "海淀基督教堂", "中国油画院", "模范书局", "东交民巷"],
          navList: [],
          ifs: "逛教堂"
        },
        {
          title: "北京中轴线景观",
          nav: ["故宫博物院", "正阳门箭楼", "天安门城楼", "鼓楼", "钟楼", "先农坛", "天坛公园", "永定门公园"],
          navList: [],
          ifs: "中轴线"
        },
        {
          title: "中轴线延伸奥林匹克风景区",
          nav: ["党史展览馆", "考古博物馆", "科学技术馆", "北京科学中心", "鸟巢", "水立方嬉水乐园", "奥林匹克塔", "国家会议中心", "科学家博物馆", "奥运博物馆", "中华民族园", "北顶娘娘庙", "奥林匹克森林公园", "奥林匹克公园", "中国工艺美术馆"],
          navList: [],
          ifs: "奥林匹克"
        },
        {
          title: "既是寺庙，也是博物馆",
          nav: ["宣南文化博物馆", "孔庙国子监", "先农坛", "万寿寺", "石刻艺术博物馆", "西黄寺", "雍和宫", "智化寺", "大钟寺", "妙音白塔寺", "历代帝王庙", "大觉寺", "太庙", "天宁寺", "东岳庙", "香山公园", "天坛公园", "八大处公园", "法海寺森林公园", "法源寺", "庆云寺"],
          navList: [],
          ifs: "寺庙博物馆"
        },
        {
          title: "逛大学里的博物馆",
          nav:
            ["北京大学", "清华大学", "北京体育大学", "北京电影学院", "中医药大学博物馆", "民族服饰博物馆", "印刷博物馆", "地质大学博物馆", "林业大学博物馆", "北京航空航天博物馆", "中法大学旧址", "北大红楼", "京师大学堂", "中央美术学院美术馆", "中国人民大学", "清华大学艺术博物馆", "传媒大学博物馆", "首都体育学院", "中央民族博物馆", "世界语言博物馆", "中国农业大学"],
          navList: [],
          ifs: "大学景点"
        },
        {
          title: "逛胡同",
          nav: ["南锣鼓巷", "烟袋斜街", "红星胡同", "史家胡同博物馆", "东四胡同博物馆", "雨儿胡同", "东交民巷", "五道营胡同", "杨梅竹斜街", "小羊宜宾胡同", "西水井胡同", "鹞儿胡同", "93号博物馆(八大胡同)", "烂缦胡同", "黄米胡同"],
          navList: [],
          ifs: "逛胡同"
        },
        {
          title: "逛名人故居四合院",
          nav: ["恭王府", "科举匾额博物馆", "蒙藏学校旧址", "文丞相祠", "梅兰芳纪念馆", "沈家本故居", "纪晓岚故居", "老舍故居", "郭沫若故居", "李大钊故居", "宋庆龄故居", "蔡元培故居", "曹雪芹故居", "史家胡同博物馆", "东四胡同博物馆", "正阳书局", "93号博物馆(八大胡同)", "林白水纪念馆", "伍连德故居", "郭守敬纪念馆", "齐白石故居", "陈独秀旧居", "福州新馆", "茅盾故居"],
          navList: [],
          ifs: "名人故居"
        },
        {
          title: "影视拍摄地",
          nav: ["盛世南宫影城", "大观园", "798艺术区", "恭王府", "颐和园", "三里屯", "颐堤港", "故宫博物院", "国贸商场", "中国大饭店"],
          navList: [],
          ifs: "影视拍摄地"
        },
        {
          title: "欣赏民俗文化",
          nav: ["首都博物馆", "宣南文化博物馆", "果园民俗博物馆", "和平菓局", "供销社展览馆", "北京市档案馆", "天桥印象博物馆", "北京人艺戏剧博物馆", "东岳庙", "史家胡同博物馆", "东四胡同博物馆", "中国工艺美术馆", "空竹博物馆", "京西五里坨民俗陈列馆"],
          navList: [],
          ifs: "民俗文化"
        },
        {
          title: "京城艺术荟萃",
          nav: ["燕京八绝博物馆", "中国工艺美术馆", "景泰蓝艺术博物馆", "紫檀博物馆"],
          navList: [],
          ifs: "京城艺术荟萃"
        },
        {
          title: "去奥莱拍照",
          nav: ["花乡奥莱", "赛特奥莱·香江小镇", "八达岭奥莱"],
          navList: [],
          ifs: "奥莱"
        },
        {
          title: "充满艺术气息的商场",
          nav: ["侨福芳草地艺术中心", "SKP-S", "DT51", "五棵松万达", "世贸天阶", "蓝色港湾", "TheBox(A座)", "三里屯", "赛特奥莱·香江小镇", "八达岭奥莱", "花乡奥莱", "国贸商场", "凯德Mall大峡谷", "银泰百货(故宫风)", "金融街购物中心"],
          navList: [],
          ifs: "艺术商场"
        },
        {
          title: "冬天去温室看花，热带雨林景观",
          nav: ["国家植物园", "荒野植物园", "世界花卉大观园"],
          navList: [],
          ifs: "冬天温室"
        },
        {
          title: "溶洞景观",
          nav: ["银狐洞", "石花洞", "龙脉熔岩温泉"],
          navList: [],
          ifs: "溶洞景观"
        },
        {
          title: "红砖风格",
          nav: ["红砖美术馆", "草场地艺术区红砖", "砖窑里"],
          navList: [],
          ifs: "溶洞景观"
        },
        {
          title: "去郊区看壮丽山河",
          nav: ["八达岭长城", "大觉寺", "八达岭野生动物园", "北京野生动物园", "大运河文化旅游区", "宋庄艺术区", "北京艺术中心", "八达岭奥莱", "昌平滨河森林公园", "明十三陵", "响水湖长城风景区", "黄花城水长城", "古北水镇", "司马台长城", "慕田峪长城", "红螺寺", "雁栖湖", "潭柘寺", "戒台寺", "居庸关长城", "白瀑寺", "周口店北京人遗址博物馆"],
          navList: [],
          ifs: "六环外"
        },
        {
          title: "北京公园年卡",
          nav: ["大观园", "北海公园", "动物园", "颐和园", "水立方嬉水乐园", "香山公园", "陶然亭公园", "玉渊潭公园", "双秀公园", "北京国际雕塑公园", "景山公园", "天坛公园", "地坛公园", "中山公园", "国家植物园"],
          navList: [],
          ifs: "公园年卡"
        },
        {
          title: "白塔青塔和高塔",
          nav: ["北海公园", "妙音白塔寺", "西黄寺", "正阳书局", "玲珑塔公园", "古塔公园", "昊天公园", "大运河文化旅游区", "石刻艺术博物馆", "国家画院美术馆", "中央电视塔", "奥林匹克塔", "北京大学"],
          navList: [],
          ifs: "白塔青塔高塔"
        },
        {
          title: "冬天室内地铁直达",
          nav: ["国家博物馆", "军事博物馆", "自然博物馆", "北京科学中心", "国家会议中心", "典籍博物馆", "天桥印象博物馆", "中国地质博物馆", "印刷博物馆", "紫檀博物馆", "韩美林艺术馆", "国家大剧院"],
          navList: [],
          ifs: "适合冬天"
        },
        {
          title: "观景台",
          nav: ["奥林匹克塔", "中央电视塔", "南苑森林湿地公园", "鸟巢"],
          navList: [],
          ifs: "登高望远"
        },
        {
          title: "酒文化",
          nav: ["红星源昇号博物馆", "茅台博物馆", "牛栏山酒厂文化苑", "永丰二锅头酒博物馆", "南路烧酒文化博物馆", "龙徽葡萄酒博物馆", "张裕爱斐堡酒庄"],
          navList: [],
          ifs: "酒文化"
        },
        {
          title: "仅工作日开放",
          nav: [ "湖广会馆", "视障文化博物馆", "中药炮制技术博物馆", "半壁店村史博物馆", "中央民族博物馆", "世界语言博物馆", "化工博物馆", "中国人民大学",  "蔡元培故居", "木偶艺术剧院博物馆","传媒大学博物馆","茅台博物馆", "林业大学博物馆", "永丰二锅头酒博物馆"],
          navList: [],
          ifs: "仅工作日开放"
        },
        {
          title: "区文化中心",
          nav: ["石景山区艺术文化中心", "回龙观文化体育中心", "昌平区文化中心", "顺义区文化中心", "丰台区文化馆", "海淀区文化馆"],
          navList: [],
          ifs: "区文化中心"
        },
        {
          title: "红色教育",
          nav: ["香山革命纪念馆", "李大钊烈士陵园", "长辛店二七纪念馆", "焦庄户地道战遗址纪念馆", "北大红楼", "京师大学堂"],
          navList: [],
          ifs: "红色教育"
        },
      ],
      first: [
        {
          title: "天安门景区",
          desc: "先逛广场建筑群，再进城楼，左中山，右太庙，然后进故宫，北门出来再去景山公园回首俯瞰故宫，拍夕阳",
          nav: ["天安门广场", "主席纪念堂", "天安门城楼", "中山公园", "太庙", "故宫博物院", "景山公园"],
          navList: [],
          ifs: "天安门景区"
        },
        {
          title: "前门景区",
          desc: "先登正阳门箭楼，在逛前门大街，西逛大栅栏老字号，沿途有方砖厂69号炸酱面，吴裕泰的抹茶冰激凌，六必居博物馆，门框胡同，钱市胡同，布鞋文化博物馆，再往西去杨梅竹斜街，八大胡同",
          nav: ["正阳门箭楼", "前门大街", "大栅栏", "方砖厂69号炸酱面", "吴裕泰", "六必居博物馆", "北京布鞋文化博物馆", "杨梅竹斜街", "93号博物馆(八大胡同)"],
          navList: [],
          ifs: "前门景区"
        },
        {
          title: "后海景区",
          desc: "先去荷花市场，再去恭王府，然后去什刹海银锭桥，烟袋斜街，出来去钟鼓楼或远望，然后去火神庙，经帽儿胡同，雨儿胡同到南锣鼓巷",
          nav: ["荷花市场", "恭王府", "后海银锭桥", "烟袋斜街", "鼓楼", "钟楼", "敕建火神庙", "雨儿胡同", "南锣鼓巷"],
          navList: [],
          ifs: "后海景区"
        },
        {
          title: "海淀经典景区",
          desc: "true",
          nav: ["北京大学", "清华大学", "圆明园", "颐和园"],
          navList: [],
          ifs: "海淀景区"
        },
        {
          title: "香山植物园一日游",
          desc: "true",
          nav: ["香山公园", "国家植物园", "健锐营演武厅", "西山森林公园"],
          navList: [],
          ifs: "香山植物园一日游"
        },
        {
          title: "奥林匹克景区",
          desc: "先去鸟巢登顶，奥运博物馆，然后看水立方，北顶娘娘庙，从中轴线",
          nav: ["鸟巢", "水立方嬉水乐园", "奥林匹克塔", "国家会议中心", "奥运博物馆", "北顶娘娘庙", "奥林匹克森林公园"],
          navList: [],
          ifs: "奥林匹克景区"
        },
        {
          title: "奥林匹克博物馆群",
          desc: "true",
          nav: ["党史展览馆", "考古博物馆", "科学技术馆", "科学家博物馆", "中国工艺美术馆"],
          navList: [],
          ifs: "奥林匹克博物馆群"
        },
        {
          title: "牛街景区",
          desc: "先去长椿苑，再去宣南文化博物馆看沉浸式表演，然后去牛街小吃街吃美食，烧饼包子羊肉串，再去法源寺看唐代建筑，最后去烂漫胡同散步",
          nav: ["宣南文化博物馆", "长椿苑", "法源寺", "牛街小吃街", "烂缦胡同"],
          navList: [],
          ifs: "牛街景区"
        },
        {
          title: "西直门CityWalk",
          desc: "true",
          nav: ["动物园", "北京天文馆", "古动物馆", "典籍博物馆", "万寿寺", "石刻艺术博物馆", "紫竹院公园", "国家图书馆"],
          navList: [],
          ifs: "西直门CityWalk"
        },
        {
          title: "国贸CityWalk",
          desc: "true",
          nav: ["SKP", "SKP-S", "中国尊", "中央广播电视总台", "国贸商场"],
          navList: [],
          ifs: "国贸CityWalk"
        },
        {
          title: "西四CityWalk",
          desc: "true",
          nav: ["中国地质博物馆", "妙音白塔寺", "历代帝王庙", "广济寺", "西什库教堂", "鲁迅博物馆", "正阳书局", "红楼公共藏书馆", "八卦掌武术文化博物馆"],
          navList: [],
          ifs: "西四CityWalk"
        },
        {
          title: "雍和宫CityWalk",
          desc: "true",
          nav: ["簋街美食街", "孔庙国子监", "雍和宫", "地坛公园", "华侨历史博物馆", "五道营胡同"],
          navList: [],
          ifs: "雍和宫CityWalk"
        },
        {
          title: "建国门CityWalk",
          desc: "true",
          nav: ["海关博物馆", "邮政邮票博物馆", "古观象台", "于谦祠", "建国门绿化广场", "中国妇女儿童博物馆", "北京国际饭店", "小羊宜宾胡同"],
          navList: [],
          ifs: "建国门CityWalk"
        },
        {
          title: "朝阳门CityWalk",
          desc: "true",
          nav: ["智化寺", "日坛公园", "朝阳门SOHO", "银河SOHO", "朝外MEN写字楼", "悠唐购物中心", "西水井胡同", "三丰里小区"],
          navList: [],
          ifs: "朝阳门CityWalk"
        },
        {
          title: "东大桥CityWalk",
          desc: "true",
          nav: ["东岳庙", "美克洞学馆", "侨福芳草地艺术中心", "TheBox(A座)", "世贸天阶", "东岳美术馆", "茑屋书店", "蓝岛大厦", "地下科幻通道"],
          navList: [],
          ifs: "东大桥CityWalk"
        },
        {
          title: "通州一日游",
          desc: "true",
          nav: ["北京大运河博物馆", "通州区博物馆", "大运河文化旅游区", "韩美林艺术馆", "北京艺术中心", "北京城市图书馆", "通州万象汇"],
          navList: [],
          ifs: "通州一日游"
        },
        {
          title: "石景山一日游",
          desc: "true",
          nav: ["首钢园", "模式口文化街", "燕京八绝博物馆", "第四纪冰川遗迹陈列馆", "法海寺森林公园", "宦官博物馆", "石景山游乐园", "五棵松万达", "华熙Live"],
          navList: [],
          ifs: "石景山一日游"
        },
        {
          title: "八达岭一日游",
          desc: "true",
          nav: ["S2线观赏花海", "八达岭长城", "八达岭野生动物园", "詹天佑纪念馆"],
          navList: [],
          ifs: "八达岭一日游"
        },

      ],
      last: [
        {
          title: "近郊景区",
          nav: [],
          navList: [
            {
              "title": "多伦湖草原",
              "img": "https://img0.baidu.com/it/u=2744580035,1098321611&fm=253&fmt=auto&app=138&f=JPEG?w=371&h=247"
            },
            {
              "title": "龙庆峡",
              "img": "https://img2.baidu.com/it/u=1580346227,3893158150&fm=253&fmt=auto&app=138&f=JPEG?w=783&h=500"
            },
            {
              "title": "青龙峡",
              "img": "https://img1.baidu.com/it/u=124649050,3040251424&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=500"
            },
            {
              "title": "玉渡山",
              "img": "https://img2.baidu.com/it/u=1527682430,940622329&fm=253&fmt=auto&app=120&f=JPEG?w=844&h=500"
            },
            {
              "title": "凤凰岭",
              "img": "https://img2.baidu.com/it/u=4140543326,1116567064&fm=253&fmt=auto&app=120&f=JPEG?w=588&h=590"
            },
            {
              "title": "十三陵水库",
              "img": "https://img1.baidu.com/it/u=2803444488,356726041&fm=253&fmt=auto&app=138&f=JPEG?w=670&h=500"
            },
            {
              "title": "易水湖景区",
              "img": "https://img0.baidu.com/it/u=3387041975,279058937&fm=253&app=138&f=JPEG?w=800&h=1067"
            },
            {
              "title": "怀柔影视基地",
              "img": "https://img1.baidu.com/it/u=315150586,1638394700&fm=253&fmt=auto&app=138&f=JPEG?w=889&h=500"
            },
            {
              "title": "虎峪自然风景区",
              "img": "https://img0.baidu.com/it/u=2001360442,2316716941&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500"
            },
            {
              "title": "京东大峡谷",
              "img": "https://img1.baidu.com/it/u=3749877184,3274072103&fm=253&fmt=auto&app=120&f=JPEG?w=949&h=635"
            },
            {
              "title": "百里山水画廊",
              "img": "https://img0.baidu.com/it/u=1173490680,252340578&fm=253&fmt=auto&app=138&f=JPEG?w=841&h=500"
            },
            {
              "title": "避暑山庄",
              "img": "https://img2.baidu.com/it/u=909195794,3982928053&fm=253&fmt=auto&app=138&f=JPEG?w=734&h=500"
            },
            {
              "title": "坡峰岭",
              "img": "https://img1.baidu.com/it/u=1424894642,2715779859&fm=253&fmt=auto&app=120&f=JPEG?w=1199&h=800"
            },
            {
              "title": "十渡风景区",
              "img": "https://img0.baidu.com/it/u=1959285834,1781382586&fm=253&fmt=auto&app=138&f=JPEG?w=750&h=499"
            },
            {
              "title": "东方普罗旺斯薰衣草庄园",
              "img": "https://img0.baidu.com/it/u=3711806389,3035238133&fm=253&fmt=auto&app=138&f=JPEG?w=600&h=400"
            },
          ],
          ifs: ""
        }
      ],
      defaultImg: '/static/images/m5.png'
    };
  },
  computed: {
    ...mapState(useGlobalStore, ['myGlobalData', 'noNet']),
    ...mapWritableState(useGlobalStore, ['selectedPlace', 'source', 'filterItem', 'navIds', 'latitude', 'longitude'])
  },
  onShow() {
    this.noNet = useGlobalStore().noNet;
  },
  onLoad() {
    this.init();
  },
  methods: {
    onRefresherRefresh() {
      setTimeout(() => {
        uni.showLoading({
          title: "加载中"
        });
        this.refresherTriggered = false;
        uni.hideLoading();
      }, 500);
    },
    init() {
      const globalData = this.myGlobalData;
      const newAll = (this.all.concat(this.first)).map(item => {
        const da = globalData.filter(subItem => item.nav.includes(subItem.title));
        const sortedDa = item.nav.map(title => da.find(subItem => subItem.title == title)).filter(Boolean);
        return {
          ...item,
          navList: sortedDa
        };
      });

      this.all = this.first.concat(newAll);
      this.loadDataInBatches(newAll, 0);

      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.latitude = res.latitude;
          this.longitude = res.longitude;
        },
        fail: (err) => {
          console.error('Location error:', err);
        }
      });
    },
    loadDataInBatches(data, startIndex) {
      const batchSize = 5;
      const endIndex = startIndex + batchSize;
      const batch = data.slice(startIndex, endIndex);

      if (batch.length > 0) {
        this.displayedList = this.displayedList.concat(batch);
        // Continue loading next batch in next tick to avoid blocking UI
        this.$nextTick(() => {
          this.loadDataInBatches(data, endIndex);
        });
      } else {
        this.displayedList = this.displayedList.concat(this.last);
      }
    },
    onPlaceTap(place, list) {
      console.warn(place);
      if (place.latitude) {
        this.selectedPlace = place;
        this.source = 'cd';
        uni.switchTab({
          url: `/pages/map/map`,
          success: function (res) {
            console.log('跳转成功');
          },
          fail: function (err) {
            console.error('跳转失败:', err);
          }
        });
      } else if (place.img) {
        if (list) {
          const da = list.map(item => item.img);
          uni.previewImage({
            urls: da,
            current: da.indexOf(place.img)
          });
        }
      }
    },
    goToMap(poi) {
      if (poi.nav.length > 0) {
        this.source = 'cd';
        this.filterItem = poi.ifs;
        this.navIds = poi.nav;
        uni.switchTab({
          url: `/pages/map/map`,
          success: function (res) {
            console.log('跳转成功');
          },
          fail: function (err) {
            console.error('跳转失败:', err);
          }
        });
      }
    },
    backToTop() {
      this.scrollTop = 0;
    },
    onImageLoad(e, index, index1) {
      // Logic from source was commented out, keeping it empty or implementation if needed
    },
    onImageError(e, index, index1) {
       // Logic from source was commented out
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
    // Helper methods from wxs
    getDesc(item) {
      if (!item.navList) return '';
      let titles = [];
      for (let i = 0; i < item.navList.length; i++) {
        let obj = item.navList[i];
        if (obj.title) {
          titles.push((i + 1) + '.' + obj.title);
        }
      }
      return titles.join(' - ');
    },
    getTitleColor(item) {
      if (item.desc) {
        return '#D3FACE';
      } else if (item.ifs && item.ifs.length > 0) {
        return 'rgb(247, 237, 223)';
      } else {
        return '#CED6FA';
      }
    }
  }
}
</script>

<style scoped>
.container {
  padding: 5px 0 5px 5px;
  overflow: hidden;
}

.title {
  font-size: 14px;
  font-weight: 500;
  margin: 3px;
  align-self: flex-start;
  color: #333;
  margin-top: -2px;
  border-radius: 4px;
  padding: 5px 8px;
  background-color: rgb(247, 237, 223);
  display: inline-block; /* Ensure background wraps text correctly */
}

.desc {
  font-size: 11px;
  font-weight: 400;
  margin-left: 3px;
  margin-top: -5px;
  margin-bottom: 3px;
  margin-right: 8px;
  align-self: flex-start;
  color: rgb(112, 111, 111);
  border-radius: 3px;
  padding: 5px;
  background-color: rgb(247, 247, 247);
  display: block; /* Typically desc takes full width or block behavior */
}

.bgimg {
  object-fit: cover;
}

.has-shadow {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.scroll-wrapper {
  width: 100%;
  height: 110px;
  overflow: hidden;
}

.scrollable-container {
  width: 100%;
  height: 130px;
  padding: 0px;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 20px; 
  margin-bottom: -20px; 
}

.scrollable-item {
  display: inline-block;
  margin-right: 3px;
  background-color: white;
  border-radius: 8px;
  vertical-align: top;
}

.content-wrapper {
  display: flex;
  flex-direction: column;  
  align-items: center;
  justify-content: center;
  padding: 3px;
  position: relative;
}

.image-content {
  width: 120px;
  height: 85px;
  object-fit: cover;
  border-radius: 5px;
}

.image-content2 {
  position: absolute;
  top:0;
  left:0;
  width: 120px;
  height: 85px;
  object-fit: cover;
  border-radius: 8px;
}

.text-content {
  font-size: 12px;
  color: rgb(97, 96, 96);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  margin-top: 5px;
  font-weight: 430;
}
</style>
