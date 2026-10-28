<template>
    <div class="main-view-container">
        <!-- 个人信息卡片 -->
        <div class="info-card">
            <div class="card-header">
                <div class="header-icon">
                    <el-icon>
                        <User />
                    </el-icon>
                </div>
                <h3 class="card-title">个人信息</h3>
            </div>

            <div class="info-content">
                <div class="info-left">
                    <el-form ref="form" :model="data.info" label-width="100px" class="base-table-form">
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-form-item label="学号">
                                    <el-input v-model="data.info.num" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="姓名">
                                    <el-input v-model="data.info.name" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="学院">
                                    <el-input v-model="data.info.dept" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-form-item label="专业">
                                    <el-input v-model="data.info.major" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="班级">
                                    <el-input v-model="data.info.name" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="证件号码">
                                    <el-input v-model="data.info.card" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-form-item label="性别">
                                    <el-input v-model="data.info.genderName" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="出生日期">
                                    <el-input v-model="data.info.birthday" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="邮箱">
                                    <el-input v-model="data.info.email" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-form-item label="电话">
                                    <el-input v-model="data.info.phone" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="地址">
                                    <el-input v-model="data.info.address" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :span="8">
                                <el-form-item label="备注">
                                    <el-input v-model="data.info.dept" readonly class="readonly-input" />
                                </el-form-item>
                            </el-col>
                        </el-row>
                    </el-form>
                </div>

                <div class="info-right">
                    <div class="avatar-container">
                        <img :src="data.imgStr" alt="个人照片" class="person-avatar" />
                        <div class="avatar-name">{{ data.info.name }}</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 成绩记录卡片 -->
        <div class="score-card">
            <div class="card-header">
                <div class="header-icon">
                    <el-icon>
                        <Document />
                    </el-icon>
                </div>
                <h3 class="card-title">成绩记录</h3>
            </div>

            <div class="table-container">
                <el-table :data="data.scoreList" class="score-table" stripe>
                    <el-table-column label="序号" width="70" align="center">
                        <template v-slot="scope">
                            <span class="sequence-number">{{ scope.$index + 1 }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="studentNum" label="学号" width="140" align="center" />
                    <el-table-column prop="studentName" label="姓名" width="100" align="center" />
                    <el-table-column prop="className" label="班级" width="200" align="center" />
                    <el-table-column prop="courseNum" label="课程号" width="140" align="center" />
                    <el-table-column prop="courseName" label="课程名" width="200" show-overflow-tooltip />
                    <el-table-column prop="credit" label="学分" width="100" align="center">
                        <template v-slot="scope">
                            <el-tag type="info" size="small">{{ scope.row.credit }}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column prop="mark" label="成绩" width="100" align="center">
                        <template v-slot="scope">
                            <el-tag
                                :type="scope.row.mark >= 90 ? 'success' : scope.row.mark >= 80 ? 'warning' : scope.row.mark >= 60 ? 'info' : 'danger'"
                                size="small">
                                {{ scope.row.mark }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </div>

        <!-- 数据分析卡片 -->
        <div class="charts-card">
            <div class="card-header">
                <div class="header-icon">
                    <el-icon>
                        <DataAnalysis />
                    </el-icon>
                </div>
                <h3 class="card-title">数据分析</h3>
            </div>

            <div class="charts-container">
                <el-row :gutter="20">
                    <el-col :span="8">
                        <div class="chart-item">
                            <div class="chart-header">
                                <h4>消费统计（柱状图）</h4>
                            </div>
                            <div id="myChartBar" class="chart-content"></div>
                        </div>
                    </el-col>
                    <el-col :span="8">
                        <div class="chart-item">
                            <div class="chart-header">
                                <h4>成绩分布（饼图）</h4>
                            </div>
                            <div id="myChartPie" class="chart-content"></div>
                        </div>
                    </el-col>
                    <el-col :span="8">
                        <div class="chart-item">
                            <div class="chart-header">
                                <h4>消费趋势（折线图）</h4>
                            </div>
                            <div id="myChartLine" class="chart-content"></div>
                        </div>
                    </el-col>
                </el-row>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup name="StudentIntroduce">
import type { StudentIntroduceData } from "~/domain/models/info";
import { container } from '~/inverfiy.config';
import { ECharts, init } from 'echarts';
import { ID_STUDENT_INTRODUCE_PRESENTER } from '~/types';
import { StudentIntroducePresenter } from "~/domain/presenters/student-introduce-presenter";
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { StudentItem } from "~/domain/models/person";
import { User, Document, DataAnalysis } from '@element-plus/icons-vue'
const presenter = container.get<StudentIntroducePresenter>(ID_STUDENT_INTRODUCE_PRESENTER);
let data = ref<StudentIntroduceData>({ info: { personId: 0 } as StudentItem } as StudentIntroduceData);
let timer: string | number | NodeJS.Timeout | undefined;
let myChartBar: ECharts | null = null;
let myChartLine: ECharts | null = null;
let myChartPie: ECharts | null = null;
const props = defineProps({
    personId: {
        type: Number,
        required: true
    },
});

const initChart = async () => {
    await presenter.studentIntroduceInit(data.value);
    drawEcharts();
};

watch(
    () => props.personId,
    val => {
        console.log("value", val);
        data.value.personId = val;
        initChart();
    },
    { immediate: true }
)

onMounted(async () => {
    await initChart();
    window.addEventListener('resize', resizeChart);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart);
    clearTimeout(timer);
    timer = 0;
});

const drawEcharts = () => {
    //绘制用户活跃图表
    if (
        myChartBar != null &&
        myChartBar != undefined
    ) {
        myChartBar.dispose(); //解决echarts dom已经加载的报错
    }
    if (
        myChartLine != null &&
        myChartLine != undefined
    ) {
        myChartLine.dispose(); //解决echarts dom已经加载的报错
    }
    if (
        myChartPie != null &&
        myChartPie != undefined
    ) {
        myChartPie.dispose(); //解决echarts dom已经加载的报错
    }
    myChartBar = init(
        document.getElementById("myChartBar") as any
    );
    myChartBar.setOption({
        title: {
            text: "日常消费统计",
            textStyle: {
                color: '#2c3e50',
                fontSize: 14,
                fontWeight: 'bold'
            }
        },
        tooltip: {
            backgroundColor: 'rgba(102, 126, 234, 0.9)',
            borderColor: '#667eea',
            textStyle: {
                color: '#fff'
            }
        },
        xAxis: {
            data: data.value.feeList.map((item) => item.title),
            axisLine: {
                lineStyle: {
                    color: '#667eea'
                }
            },
            axisLabel: {
                color: '#666'
            }
        },
        yAxis: {
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
                name: "消费",
                type: "bar",
                data: data.value.feeList.map((item) => item.value),
                itemStyle: {
                    color: {
                        type: 'linear',
                        x: 0,
                        y: 0,
                        x2: 0,
                        y2: 1,
                        colorStops: [{
                            offset: 0, color: '#667eea'
                        }, {
                            offset: 1, color: '#764ba2'
                        }]
                    }
                },
                barWidth: '60%'
            },
        ],
    });
    myChartLine = init(
        document.getElementById("myChartLine") as any
    );
    myChartLine.setOption({
        title: {
            text: "消费趋势分析",
            textStyle: {
                color: '#2c3e50',
                fontSize: 14,
                fontWeight: 'bold'
            }
        },
        tooltip: {
            backgroundColor: 'rgba(102, 126, 234, 0.9)',
            borderColor: '#667eea',
            textStyle: {
                color: '#fff'
            }
        },
        xAxis: {
            data: data.value.feeList.map((item) => item.title),
            axisLine: {
                lineStyle: {
                    color: '#667eea'
                }
            },
            axisLabel: {
                color: '#666'
            }
        },
        yAxis: {
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
                name: "消费",
                type: "line",
                data: data.value.feeList.map((item) => item.value),
                itemStyle: {
                    color: '#4facfe'
                },
                lineStyle: {
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
                    },
                    width: 3
                },
                symbol: 'circle',
                symbolSize: 8
            },
        ],
    });
    myChartPie = init(
        document.getElementById("myChartPie") as any
    );
    myChartPie.setOption({
        title: {
            text: "成绩分布统计",
            textStyle: {
                color: '#2c3e50',
                fontSize: 14,
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
            orient: "horizontal",
            x: "center",
            y: "bottom",
            data: data.value.markList.map((item) => item.title),
            textStyle: {
                color: '#666'
            }
        },
        color: [
            '#667eea', '#764ba2', '#f093fb', '#f5576c',
            '#4facfe', '#00f2fe', '#43e97b', '#38f9d7'
        ],
        series: [
            {
                name: "成绩分布",
                type: "pie",
                radius: ['40%', '70%'],
                center: ['50%', '50%'],
                data: data.value.markList,
                itemStyle: {
                    borderRadius: 6,
                    borderWidth: 2,
                    borderColor: '#fff'
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(102, 126, 234, 0.5)'
                    }
                }
            },
        ],
    });
};
const resizeChart = (): void => {
    timer = setTimeout(() => {
        if (myChartBar) {
            myChartBar.resize();
        }
        if (myChartLine) {
            myChartLine.resize();
        }
        if (myChartPie) {
            myChartPie.resize();
        }
    }, 500);
};

</script>
<style lang="scss" scoped>
/* 卡片通用样式 */
.info-card,
.score-card,
.charts-card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
    margin-bottom: 24px;
    border: 1px solid rgba(102, 126, 234, 0.1);
    transition: all 0.3s ease;
    overflow: hidden;

    &:hover {
        box-shadow: 0 8px 30px rgba(102, 126, 234, 0.2);
        transform: translateY(-2px);
    }
}

/* 卡片头部样式 */
.card-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 16px 24px;
    display: flex;
    align-items: center;
    border-bottom: none;

    .header-icon {
        width: 32px;
        height: 32px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 12px;
        color: white;
        font-size: 16px;
    }

    .card-title {
        color: white;
        font-size: 18px;
        font-weight: 600;
        margin: 0;
        flex: 1;
    }
}

/* 个人信息卡片内容 */
.info-content {
    padding: 24px;
    display: flex;
    gap: 30px;
}

.info-left {
    flex: 1;
}

.info-right {
    width: 200px;
    display: flex;
    justify-content: center;
    align-items: flex-start;
}

.avatar-container {
    text-align: center;
}

.student-avatar {
    width: 160px;
    height: 200px;
    border-radius: 12px;
    object-fit: cover;
    border: 4px solid #f0f2f5;
    box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);
    transition: all 0.3s ease;

    &:hover {
        transform: scale(1.05);
        box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
    }
}

.avatar-name {
    margin-top: 12px;
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

/* 表单样式 */
.student-form {
    :deep(.el-form-item__label) {
        color: #606266;
        font-weight: 500;
    }

    :deep(.el-form-item) {
        margin-bottom: 20px;
    }
}

.readonly-input {
    :deep(.el-input__wrapper) {
        background: #f8f9fa;
        border: 1px solid #e9ecef;
        box-shadow: none;
        transition: all 0.3s ease;

        &:hover {
            border-color: #667eea;
            background: #f0f2ff;
        }
    }

    :deep(.el-input__inner) {
        color: #495057;
        font-weight: 500;
    }
}

/* 成绩表格样式 */
.table-container {
    padding: 24px;
}

.score-table {
    :deep(.el-table__header) {
        background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);

        th {
            background: transparent !important;
            color: #2c3e50;
            font-weight: 600;
            border-bottom: 2px solid #667eea;
        }
    }

    :deep(.el-table__row) {
        transition: all 0.3s ease;

        &:hover {
            background: linear-gradient(135deg, #f0f2ff 0%, #e6f0ff 100%) !important;
        }
    }

    :deep(.el-table td) {
        border-bottom: 1px solid #f0f2f5;
        padding: 12px 0;
    }
}

.sequence-number {
    width: 24px;
    height: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
}

/* 图表区域样式 */
.charts-container {
    padding: 24px;
}

.chart-item {
    background: #ffffff;
    border-radius: 8px;
    border: 1px solid #f0f2f5;
    overflow: hidden;
    transition: all 0.3s ease;

    &:hover {
        box-shadow: 0 4px 16px rgba(102, 126, 234, 0.15);
        transform: translateY(-2px);
    }
}

.chart-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    padding: 12px 16px;
    border-bottom: 1px solid #f0f2f5;

    h4 {
        margin: 0;
        color: #2c3e50;
        font-size: 14px;
        font-weight: 600;
    }
}

.chart-content {
    width: 100%;
    height: 280px;
    padding: 16px;
    background: #ffffff;
}

/* 图表容器样式 */
#myChartBar,
#myChartPie,
#myChartLine {
    background: #ffffff;
    border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .info-content {
        flex-direction: column;
    }

    .info-right {
        width: 100%;
        justify-content: center;
    }

    .charts-container {
        :deep(.el-col) {
            margin-bottom: 20px;
        }
    }
}

@media (max-width: 768px) {
    .student-introduce-container {
        padding: 12px;
    }

    .info-content,
    .table-container,
    .charts-container {
        padding: 16px;
    }

    .student-avatar {
        width: 120px;
        height: 150px;
    }

    .chart-content {
        height: 200px;
        padding: 8px;
    }

    :deep(.el-col) {
        margin-bottom: 16px;
    }
}

/* Element Plus 组件样式覆盖 */
:deep(.el-table) {
    border-radius: 8px;
    overflow: hidden;
}

:deep(.el-tag) {
    border-radius: 6px;
    font-weight: 500;
}

:deep(.el-row) {
    margin-bottom: 0 !important;
}
</style>