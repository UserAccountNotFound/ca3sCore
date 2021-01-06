import { Component, Vue, Inject } from 'vue-property-decorator';

import { numeric, required, minLength, maxLength } from 'vuelidate/lib/validators';

import ProtectedContentService from '../protected-content/protected-content.service';
import { IProtectedContent } from '@/shared/model/protected-content.model';

import AlertService from '@/shared/alert/alert.service';
import { IRequestProxyConfig, RequestProxyConfig } from '@/shared/model/request-proxy-config.model';
import RequestProxyConfigService from './request-proxy-config.service';

const validations: any = {
  requestProxyConfig: {
    name: {
      required,
    },
    requestProxyUrl: {
      required,
    },
    active: {},
  },
};

@Component({
  validations,
})
export default class RequestProxyConfigUpdate extends Vue {
  @Inject('alertService') private alertService: () => AlertService;
  @Inject('requestProxyConfigService') private requestProxyConfigService: () => RequestProxyConfigService;
  public requestProxyConfig: IRequestProxyConfig = new RequestProxyConfig();

  @Inject('protectedContentService') private protectedContentService: () => ProtectedContentService;

  public protectedContents: IProtectedContent[] = [];
  public isSaving = false;
  public currentLanguage = '';

  beforeRouteEnter(to, from, next) {
    next(vm => {
      if (to.params.requestProxyConfigId) {
        vm.retrieveRequestProxyConfig(to.params.requestProxyConfigId);
      }
      vm.initRelationships();
    });
  }

  created(): void {
    this.currentLanguage = this.$store.getters.currentLanguage;
    this.$store.watch(
      () => this.$store.getters.currentLanguage,
      () => {
        this.currentLanguage = this.$store.getters.currentLanguage;
      }
    );
  }

  public save(): void {
    this.isSaving = true;
    if (this.requestProxyConfig.id) {
      this.requestProxyConfigService()
        .update(this.requestProxyConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestProxyConfig.updated', { param: param.id });
          this.alertService().showAlert(message, 'info');
        });
    } else {
      this.requestProxyConfigService()
        .create(this.requestProxyConfig)
        .then(param => {
          this.isSaving = false;
          this.$router.go(-1);
          const message = this.$t('ca3SApp.requestProxyConfig.created', { param: param.id });
          this.alertService().showAlert(message, 'success');
        });
    }
  }

  public retrieveRequestProxyConfig(requestProxyConfigId): void {
    this.requestProxyConfigService()
      .find(requestProxyConfigId)
      .then(res => {
        this.requestProxyConfig = res;
      });
  }

  public previousState(): void {
    this.$router.go(-1);
  }

  public initRelationships(): void {
    this.protectedContentService()
      .retrieve()
      .then(res => {
        this.protectedContents = res.data;
      });
  }
}
