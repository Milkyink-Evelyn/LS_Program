export interface DataRequest {
    data: any;
  }
  export interface DataResponse {
    data: any;
    code: number;
    msg: string;
  }
  export interface ValidateCode {
    validateCodeId: number;
    img: string;
  }
