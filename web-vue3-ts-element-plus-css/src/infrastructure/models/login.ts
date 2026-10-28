export interface LoginRequest {
    username: string;
    password: string;
    code: string;

}
export interface JwtResponse {
    token: string;
    type: string;
    id: number;
    username: string;
    perName: string;
    role: string;
}

