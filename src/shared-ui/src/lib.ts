// This file is the entry point for the library build
import { VueConstructor } from 'vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import Expandable from './components/Expandable.vue';
import FileUpload from './components/FileUpload.vue';
import FormLabel from './components/FormLabel.vue';
import HelpIcon from './components/HelpIcon.vue';
import LargeButton from './components/LargeButton.vue';
import ProgressLinear from './components/ProgressLinear.vue';
import SimpleDialog from './components/SimpleDialog.vue';
import SmallButton from './components/SmallButton.vue';
import StatusIcon from './components/StatusIcon.vue';
import SubViewFooter from './components/SubViewFooter.vue';
import SubViewTitle from './components/SubViewTitle.vue';
// Import vee-validate so it's configured on the library build
import './plugins/vee-validate';
import './i18n';

const SharedComponents = {
  install(Vue: VueConstructor): void {
    Vue.component('ConfirmDialog', ConfirmDialog);
    Vue.component('Expandable', Expandable);
    Vue.component('FileUpload', FileUpload);
    Vue.component('FormLabel', FormLabel);
    Vue.component('HelpIcon', HelpIcon);
    Vue.component('LargeButton', LargeButton);
    Vue.component('ProgressLinear', ProgressLinear);
    Vue.component('SimpleDialog', SimpleDialog);
    Vue.component('SmallButton', SmallButton);
    Vue.component('StatusIcon', StatusIcon);
    Vue.component('SubViewFooter', SubViewFooter);
    Vue.component('SubViewTitle', SubViewTitle);
  },
};

export default SharedComponents;
