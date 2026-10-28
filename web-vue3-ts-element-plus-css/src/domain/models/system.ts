
import { OptionItem } from '~/infrastructure/models/base';
export interface TreeNode {
    pid: number | null;
    id: number;
    value: string;
    title: string;
    label: string;
    userTypeIds: string;
    parentTitle: string;
    children: TreeNode[];
    isLeaf: number | null;
}
export interface DictionaryManageData {
    nodes: TreeNode[];
    currentNode: TreeNode;
    defaultProps: any;
}

export interface MenuManageData {
    nodes: TreeNode[];
    currentNode: TreeNode;
    roleList: OptionItem[];
    defaultProps: any;
    readonly: boolean;
    editType: number;
    roleAdmin: boolean;
    roleStudent: boolean;
    roleTeacher: boolean;
} 