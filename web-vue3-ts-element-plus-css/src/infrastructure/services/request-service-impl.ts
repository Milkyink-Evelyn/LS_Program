import axios, { type RawAxiosRequestHeaders } from "axios";
import { IRequestService } from "../boundaries/request-service";
import { IStoreService } from "~/infrastructure/boundaries/store-service";
import { ID_STORE_SERVICE } from "~/types";
import { inject, injectable } from "inversify";

@injectable()
export class RequestServiceImpl implements IRequestService {
  constructor(
    @inject(ID_STORE_SERVICE) private readonly storeService: IStoreService
  ) { }

  public getAuthHeader(): any {
    return {
      Authorization: "Bearer " + this.storeService.getToken(),
    };
  }
  getAuthHeaderJwt(jwt: string): any {
    return {
      Authorization: "Bearer " + jwt,
    };
  }

  getAuthHeaderFile(): RawAxiosRequestHeaders {
    return {
      Authorization: "Bearer " + this.storeService.getToken(),
      "Content-Type": "multipart/form-data",
    };
  }
  public async generalRequest(
    url: string,
    data: any | null
  ): Promise<any> {
    try {
      const res = await axios.post(
        url,
        {
          data: data,
        },
        {
          headers: this.getAuthHeader(),
        }
      );
      if (res.status != 200) {
        console.log("Error: " + res.status);
        return;
      }
      return res.data;
    } catch (error) {
      console.log("Error: " + error);
      //    useAppStore().logout();
      //    router.push("/login");
    }
  }
  public async jwtRequest(
    jwt: string,
    url: string,
    data: any | null
  ): Promise<any> {
    try {
      const res = await axios.post(
        url,
        {
          data: data,
        },
        {
          headers: this.getAuthHeaderJwt(jwt),
        }
      );
      if (res.status != 200) {
        console.log("Error: " + res.status);
        return;
      }
      return res.data;
    } catch (error) {
      //          console.log("Error: " + error);
      //          useAppStore().logout();
      //          router.push({ path: "/login" });
    }
  }

  public async uploadRequest(
    url: string,
    data: any | null
  ): Promise<any> {
    const res = await axios.post(url, data, {
      headers: this.getAuthHeaderFile(),
    });
    if (res.status != 200) {
      console.log("Error: " + res.status);
      return;
    }
    return res.data;
  }

  public async downloadPost(url: string, label: string, data: any): Promise<any> {
    const requestOptions = {
      method: "POST",
      headers: {
        "content-type": "application/json",
        Authorization: "Bearer " + this.storeService.getToken(),
      },
      body: JSON.stringify({
        data: data,
      }),
    };
    const res = await fetch(url, requestOptions)
      .then(async (response) => {
        const blob = await response.blob();

        // check for error response
        if (!response.ok) {
          // get error message from body or default to response status
          const error = response.status;
          return Promise.reject(error);
        }
        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);
        link.download = label;
        link.click();
        URL.revokeObjectURL(link.href);
        return Promise.resolve(response.status);
      })
      .catch((error) => {
        console.error("There was an error!", error);
      });
    return res;
  }

  public async getPdfData(url: string, data: any): Promise<string> {
    const requestOptions = {
      method: "POST",
      headers: {
        "content-type": "application/json",
        Authorization: "Bearer " + this.storeService.getToken(),
      },
      body: JSON.stringify({
        data: data,
      }),
    };
    const res = await fetch(url, requestOptions)
      .then(async (response) => {
        const blob = await response.blob();

        // check for error response
        if (!response.ok) {
          // get error message from body or default to response status
          const error = response.status;
          return Promise.reject(error);
        }
        let frameUrl = URL.createObjectURL(blob);
        return Promise.resolve(frameUrl);
      })
      .catch((error) => {
        console.error("There was an error!", error);
      });
    return res as string;
  }
  public async doExport(url: string, fileName: string, data: any): Promise<any> {
    const res = await this.downloadPost(url, fileName, data);
    if (res != 200) {
      alert("导出失败！");
    }
  }
}