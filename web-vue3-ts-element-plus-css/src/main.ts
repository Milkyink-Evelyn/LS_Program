import { createApp } from "vue";
import App from "./App.vue";
import pinia from "./stores";

// import "~/styles/element/index.scss";

import ElementPlus from "element-plus";
// import all element css, uncommented next line
import "element-plus/dist/index.css";

// or use cdn, uncomment cdn link in `index.html`

import "~/styles/index.scss";
import "uno.css";
import router from "./router";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import * as echarts from 'echarts';

// If you want to use ElMessage, import it.
import "element-plus/theme-chalk/src/message.scss";

const app = createApp(App);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component);
}
app.use(pinia);
app.use(ElementPlus);
app.use(router);
app.mount("#app");
app.config.globalProperties.$echarts = echarts;