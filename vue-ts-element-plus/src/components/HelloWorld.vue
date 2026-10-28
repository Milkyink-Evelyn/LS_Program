<script lang="ts">
import { mapState } from "pinia";
import { defineComponent } from "vue";
import { useAppStore } from "../store/app";
import { userInfo } from "os";

export default defineComponent({
  data() {
    return {
      open: ["Users"],
      admins: [
        ["Management", "mdi-account-multiple-outline"],
        ["Settings", "mdi-cog-outline"],
      ],
      cruds: [
        ["Create", "mdi-plus-outline"],
        ["Read", "mdi-file-outline"],
        ["Update", "mdi-update"],
        ["Delete", "mdi-delete"],
      ],
    };
  },
  computed: {
    ...mapState(useAppStore, ["systemConfig", "userInfo"]),
  },
  methods: {
    click: (e: any) => {
      const store = useAppStore();
      store.login("admin12", "admin");
      //   .then(() => {
      //   store.$persist();
      // });
      // console.log(e);
    },
  },
});
</script>

<template>
  <el-button v-on:click="click">{{ userInfo.username }}</el-button>
</template>

<style>
.ep-button {
  margin: 4px;
}
.ep-button + .ep-button {
  margin-left: 0;
  margin: 4px;
}
</style>
