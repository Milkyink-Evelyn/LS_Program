<template>
    <div class="main-page-container">
        <el-row :gutter="24">
            <el-col :span="12">
                <div class="chart-card main-left">
                    <div class="chart-header">
                        <img class="header-icon" src="/user.png" />
                        <div class="chart-title">活跃用户</div>
                    </div>
                    <div class="pieChart">
                        <div id="onlineUserChart" style="width: 100%; height: 275px" />
                    </div>
                </div>
            </el-col>
            <el-col :span="12">
                <div class="chart-card">
                    <div class="chart-header">
                        <img class="header-icon" src="/user.png" />
                        <div class="headerTwo">用户分类</div>
                    </div>
                    <div class="pieChart">
                        <div id="userTypeChart" style="width: 100%; height: 275px" />
                    </div>
                </div>
            </el-col>
        </el-row>
    </div>
</template>
<script lang="ts" setup name="MainPage">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ECharts, init } from 'echarts';
import type { MainPageData } from "~/infrastructure/models/base";
import { container } from '~/inverfiy.config';
import { ID_MAIN_PAGE_PRESENTER } from '~/types';
import { MainPagePresenter } from "~/infrastructure/presenters/main-page-presenter";
const presenter = container.get<MainPagePresenter>(ID_MAIN_PAGE_PRESENTER);
let data = ref<MainPageData>({} as MainPageData);
let timer: string | number | NodeJS.Timeout | undefined;
let onlineUserChart: ECharts | null = null;
let userTypeChart: ECharts | null = null;

onMounted(async () => {
    await initChart();
    window.addEventListener('resize', resizeChart);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart);
    clearTimeout(timer);
    timer = 0;
});
const initChart = async () => {
    data.value = await presenter.mainPageInit();
    drawEcharts();
};
const drawEcharts = () => {
    //绘制用户活跃图表
    if (
        onlineUserChart != null &&
        onlineUserChart != undefined
    ) {
        onlineUserChart.dispose(); //解决echarts dom已经加载的报错
    }
    if (
        userTypeChart != null &&
        userTypeChart != undefined
    ) {
        userTypeChart.dispose(); //解决echarts dom已经加载的报错
    }
    onlineUserChart = init(
        document.getElementById("onlineUserChart") as any
    );
    onlineUserChart.setOption({
        title: {
            text: "用户活跃情况",
            left: "center",
            textStyle: {
                color: '#2c3e50',
                fontSize: 16,
                fontWeight: 'bold'
            }
        },
        xAxis: {
            type: "value",
            axisLine: {
                lineStyle: {
                    color: '#667eea'
                }
            },
            axisLabel: {
                color: '#666'
            }
        },
        tooltip: {
            trigger: "axis",
            axisPointer: {
                type: "shadow",
            },
            backgroundColor: 'rgba(102, 126, 234, 0.9)',
            borderColor: '#667eea',
            textStyle: {
                color: '#fff'
            }
        },
        grid: {
            left: "3%",
            right: "4%",
            bottom: "3%",
            containLabel: true,
        },
        yAxis: {
            type: "category",
            data: ["总用户", "本月活动用户", "当日活动用户"],
            axisTick: {
                alignWithLabel: true,
            },
            axisLine: {
                lineStyle: {
                    color: '#667eea'
                }
            },
            axisLabel: {
                color: '#666'
            }
        },
        series: [
            {
                itemStyle: {
                    label: {
                        show: true, //开启显示
                        position: "right", //在上方显示
                        textStyle: {
                            //数值样式
                            color: "white",
                            fontSize: 14,
                            fontWeight: 'bold'
                        },
                    },
                },
                barWidth: "50%",
                data: [
                    {
                        value: data.value.onlineUser.total,
                        itemStyle: {
                            color: {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 1,
                                y2: 0,
                                colorStops: [{
                                    offset: 0, color: '#667eea'
                                }, {
                                    offset: 1, color: '#764ba2'
                                }]
                            }
                        },
                    },
                    {
                        value: data.value.onlineUser.monthCount,
                        itemStyle: {
                            color: {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 1,
                                y2: 0,
                                colorStops: [{
                                    offset: 0, color: '#f093fb'
                                }, {
                                    offset: 1, color: '#f5576c'
                                }]
                            }
                        },
                    },
                    {
                        value: data.value.onlineUser.dayCount,
                        itemStyle: {
                            color: {
                                type: 'linear',
                                x: 0,
                                y: 0,
                                x2: 1,
                                y2: 0,
                                colorStops: [{
                                    offset: 0, color: '#4facfe'
                                }, {
                                    offset: 1, color: '#00f2fe'
                                }]
                            }
                        },
                    },
                ],
                type: "bar",
            },
        ],
    });
    //      绘制用户分类图表
    userTypeChart = init(
        document.getElementById("userTypeChart") as any
    );
    userTypeChart.setOption({
        title: {
            text: "用户总数:" + data.value.onlineUser.total,
            left: "center",
            textStyle: {
                color: '#2c3e50',
                fontSize: 16,
                fontWeight: 'bold'
            }
        },
        tooltip: {
            trigger: "item",
            formatter: "{a} <br/>{b}: {c} ({d}%)",
            backgroundColor: 'rgba(102, 126, 234, 0.9)',
            borderColor: '#667eea',
            textStyle: {
                color: '#fff'
            }
        },
        legend: {
            top: "bottom",
            textStyle: {
                color: '#666'
            }
        },
        color: [
            '#667eea', '#764ba2', '#f093fb', '#f5576c',
            '#4facfe', '#00f2fe', '#43e97b', '#38f9d7',
            '#ffecd2', '#fcb69f', '#a8edea', '#fed6e3'
        ],
        series: [
            {
                itemStyle: {
                    borderRadius: 6,
                    borderWidth: 2,
                    borderColor: '#fff',
                    label: {
                        show: true,
                        formatter: "{b}: {c}",
                        fontSize: 12,
                        fontWeight: 'bold'
                    },
                    labelLine: {
                        show: true,
                        lineStyle: {
                            color: '#999'
                        }
                    },
                },
                name: "用户分类",
                type: "pie",
                radius: [30, 120],
                center: ["50%", "50%"],
                roseType: "area",
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(102, 126, 234, 0.5)'
                    }
                },
                data: data.value.userTypeList,
            },
        ],
    });
    //绘制请求图表
};
const resizeChart = (): void => {
    timer = setTimeout(() => {
        if (onlineUserChart) {
            onlineUserChart.resize();
        }
        if (userTypeChart) {
            userTypeChart.resize();
        }
    }, 500);
};
</script>

<style lang="scss" scoped>
/* 卡片容器样式 */
.chart-card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
    padding: 24px;
    margin-bottom: 20px;
    border: 1px solid rgba(102, 126, 234, 0.1);
    transition: all 0.3s ease;
    height: 380px;
    /* 固定高度 */

    &:hover {
        box-shadow: 0 8px 30px rgba(102, 126, 234, 0.2);
        transform: translateY(-2px);
    }
}

/* 图表头部样式 - 标题和图标在一行 */
.chart-header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid #f0f2f5;
}

.header-icon {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    padding: 6px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
    margin-right: 12px;
    transition: all 0.3s ease;

    &:hover {
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }
}

/* 标题样式 */
.chart-title {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    flex: 1;
}

.headerTwo {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0;
    flex: 1;
}

/* 图表容器 */
.pieChart {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 10px;
    background: #fafbfc;
    border-radius: 8px;
    border: 2px solid #f0f2f5;
    flex: 1;

    >div {
        border-radius: 8px;
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
}

/* 移除不需要的样式 */
.image {
    display: none;
}

.imageOne {
    display: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
    .chart-card {
        padding: 16px;
        height: auto;
    }

    .pieChart>div {
        width: 100% !important;
        height: 150px !important;
    }

    .chart-title,
    .headerTwo {
        font-size: 14px;
    }

    .header-icon {
        width: 28px;
        height: 28px;
    }
}

/* 行间距优化 */
:deep(.el-row) {
    margin-bottom: 0 !important;
}

:deep(.el-col) {
    margin-bottom: 20px;
}
</style>
