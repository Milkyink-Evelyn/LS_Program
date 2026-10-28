import { PasswordData } from "~/domain/models/info";
import { ID_INFO_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { IInfoService } from "~/domain/boundaries/info-service";
import { inject, injectable } from "inversify";
@injectable()
export class PasswordPresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_INFO_SERVICE) private readonly service: IInfoService
    ) { }
    public async submit(data: PasswordData): Promise<void> {
        var msg = "";
        if (data.oldPassword === undefined || data.oldPassword === "") {
            msg = "旧密码为空不能修改";
        } else if (data.newPassword === undefined || data.newPassword === "") {
            msg = "新密码为空不能修改";
        } else if (data.oldPassword === data.newPassword) {
            msg = "新密码和旧密码相同，不能修改";
        } else if (data.checkPassword !== data.newPassword) {
            msg = "新密码和确认密码不相同，不能修改";
        } else {
            var c;
            var ch = false;
            var num = false;
            var other = false;
            for (var i = 0; i < data.newPassword.length; i++) {
                c = data.newPassword.charAt(i);
                if ((c >= "a" && c <= "z") || (c >= "A" && c <= "Z")) {
                    ch = true;
                } else if (c >= "0" && c <= "9") {
                    num = true;
                } else {
                    other = true;
                }
            }
            if (!ch || !num || !other) {
                msg =
                    "密码至少包含大写字母、小写字母、数字和符号两种以上的类型，请重新输入！";
            } else if (data.newPassword.length < 8) {
                msg = "密码长度必须大于等于8个字符，请重新输入！";
            }
        }
        if (msg !== "") {
            this.messageService.warning(msg);
        } else {
            const response = await this.service.updatePassword({
                oldPassword: data.oldPassword,
                newPassword: data.newPassword,
            });
            if (response.code == 0) {
                this.messageService.success("提交成功");
            } else {
                this.messageService.error(response.msg);
            }
        }
    };
    public async reset(data: PasswordData): Promise<void> {
        data.oldPassword = "";
        data.newPassword = "";
        data.checkPassword = "";
    }
}