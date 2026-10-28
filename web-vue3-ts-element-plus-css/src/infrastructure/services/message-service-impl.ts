import { ElMessage, ElMessageBox } from "element-plus";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
export class MessageServiceImpl implements IMessageService {
    public success(msg: string): void {
        ElMessage.success(msg);
    }
    public info(msg: string): void {
        ElMessage.info(msg);
    }
    public error(msg: string): void {
        ElMessage.error(msg);
    }
    public warning(msg: string): void {
        ElMessage.warning(msg);
    }
    public message(type: string, message: string): void {
        switch (type) {
            case "success":
                this.success(message);
                break;
            case "info":
                this.info(message);
                break;
            case "error":
                this.error(message);
                break;
            case "warning":
                this.warning(message);
                break;
            default:
                this.info(message);
                break;
        }
    }
    //确认消息提示，用户页面显示确认消息，用户点击确认后返回true，否则返回false
    public async confirm(content: any): Promise<boolean> {
        return new Promise((resolve, reject) => {
            ElMessageBox.confirm(content, "提示", {
                confirmButtonText: "确定",
                cancelButtonText: "取消",
                type: "warning",
            })
                .then(() => {
                    resolve(true);
                })
                .catch(() => {
                    ElMessage({
                        type: "info",
                        message: "已取消删除",
                    });
                    reject(false);
                });
        });
    }
}
