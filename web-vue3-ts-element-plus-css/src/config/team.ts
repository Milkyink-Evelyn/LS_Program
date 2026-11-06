export interface TeamMember {
  name: string;
  studentId: string;
  duty: string;
  avatar?: string;
}

/**
 * 团队成员信息：请组员按实际情况替换姓名、学号和分工。
 * 这里保留占位内容，不伪造真实成员信息。
 */
export const teamMembers: TeamMember[] = [
  { name: "李思锐", studentId: "202500550052", duty: "后端接口与数据库" },
  { name: "史耘豪", studentId: "202500101116", duty: "前端页面与交互" },
];
