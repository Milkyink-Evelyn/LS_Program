export interface IMessageService {
    success(msg: string): void;
    info(msg: string): void;
    warning(msg: string): void;
    error(msg: string): void;
    message(type: string, message: string): void;
    confirm(content: any): Promise<boolean>;
}
