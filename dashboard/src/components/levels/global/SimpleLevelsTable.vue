/*
Copyright 2020 SkillTree

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
<template>
  <div id="simple-levels-table" v-if="this.levels && this.levels">

    <skills-b-table :options="table.options" :items="levels" data-cy="simpleLevelsTable">
      <template #cell(edit)="data">
        <button v-on:click="onDeleteEvent(data.item)" class="btn btn-sm btn-outline-primary"
                :aria-label="`delete level ${data.item.level} from ${data.item.projectId}`"
                :data-cy="`deleteLevelBtn_${data.item.projectId}-${data.item.level}`">
          <i class="fas fa-trash text-warning" aria-hidden="true"/>
        </button>
      </template>

    </skills-b-table>
  </div>
</template>

<script>
  import SkillsBTable from '../../utils/table/SkillsBTable';

  export default {
    name: 'SimpleLevelsTable',
    components: { SkillsBTable },
    props: ['levels'],
    data() {
      return {
        table: {
          options: {
            busy: false,
            bordered: false,
            outlined: true,
            stacked: 'md',
            fields: [
              {
                key: 'projectName',
                label: 'Project Name',
                sortable: true,
              },
              {
                key: 'level',
                label: 'Level',
                sortable: true,
              },
              {
                key: 'edit',
                label: 'Delete',
                sortable: false,
              },
            ],
            pagination: {
              currentPage: 1,
              totalRows: 1,
              pageSize: 5,
              possiblePageSizes: [5, 10, 15, 20],
            },
          },
        },
      };
    },
    methods: {
      onDeleteEvent(level) {
        this.$emit('level-removed', level);
      },
    },
  };
</script>

<style>
</style>
