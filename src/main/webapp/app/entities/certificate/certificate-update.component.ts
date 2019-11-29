import { Component, OnInit } from '@angular/core';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
// eslint-disable-next-line @typescript-eslint/no-unused-vars
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { JhiAlertService, JhiDataUtils } from 'ng-jhipster';
import { ICertificate, Certificate } from 'app/shared/model/certificate.model';
import { CertificateService } from './certificate.service';
import { ICSR } from 'app/shared/model/csr.model';
import { CSRService } from 'app/entities/csr/csr.service';

@Component({
  selector: 'jhi-certificate-update',
  templateUrl: './certificate-update.component.html'
})
export class CertificateUpdateComponent implements OnInit {
  isSaving: boolean;

  csrs: ICSR[];

  certificates: ICertificate[];
  validFromDp: any;
  validToDp: any;
  contentAddedAtDp: any;
  revokedSinceDp: any;

  editForm = this.fb.group({
    id: [],
    tbsDigest: [null, [Validators.required]],
    subject: [null, [Validators.required]],
    issuer: [null, [Validators.required]],
    type: [null, [Validators.required]],
    description: [],
    subjectKeyIdentifier: [],
    authorityKeyIdentifier: [],
    fingerprint: [],
    serial: [null, [Validators.required]],
    validFrom: [null, [Validators.required]],
    validTo: [null, [Validators.required]],
    creationExecutionId: [],
    contentAddedAt: [],
    revokedSince: [],
    revocationReason: [],
    revoked: [],
    revocationExecutionId: [],
    content: [null, [Validators.required]],
    csr: [],
    issuingCertificate: []
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected jhiAlertService: JhiAlertService,
    protected certificateService: CertificateService,
    protected cSRService: CSRService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ certificate }) => {
      this.updateForm(certificate);
    });
    this.cSRService
      .query({ filter: 'certificate-is-null' })
      .pipe(
        filter((mayBeOk: HttpResponse<ICSR[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICSR[]>) => response.body)
      )
      .subscribe(
        (res: ICSR[]) => {
          if (!this.editForm.get('csr').value || !this.editForm.get('csr').value.id) {
            this.csrs = res;
          } else {
            this.cSRService
              .find(this.editForm.get('csr').value.id)
              .pipe(
                filter((subResMayBeOk: HttpResponse<ICSR>) => subResMayBeOk.ok),
                map((subResponse: HttpResponse<ICSR>) => subResponse.body)
              )
              .subscribe((subRes: ICSR) => (this.csrs = [subRes].concat(res)), (subRes: HttpErrorResponse) => this.onError(subRes.message));
          }
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
    this.certificateService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<ICertificate[]>) => mayBeOk.ok),
        map((response: HttpResponse<ICertificate[]>) => response.body)
      )
      .subscribe((res: ICertificate[]) => (this.certificates = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(certificate: ICertificate) {
    this.editForm.patchValue({
      id: certificate.id,
      tbsDigest: certificate.tbsDigest,
      subject: certificate.subject,
      issuer: certificate.issuer,
      type: certificate.type,
      description: certificate.description,
      subjectKeyIdentifier: certificate.subjectKeyIdentifier,
      authorityKeyIdentifier: certificate.authorityKeyIdentifier,
      fingerprint: certificate.fingerprint,
      serial: certificate.serial,
      validFrom: certificate.validFrom,
      validTo: certificate.validTo,
      creationExecutionId: certificate.creationExecutionId,
      contentAddedAt: certificate.contentAddedAt,
      revokedSince: certificate.revokedSince,
      revocationReason: certificate.revocationReason,
      revoked: certificate.revoked,
      revocationExecutionId: certificate.revocationExecutionId,
      content: certificate.content,
      csr: certificate.csr,
      issuingCertificate: certificate.issuingCertificate
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  setFileData(event, field: string, isImage) {
    return new Promise((resolve, reject) => {
      if (event && event.target && event.target.files && event.target.files[0]) {
        const file: File = event.target.files[0];
        if (isImage && !file.type.startsWith('image/')) {
          reject(`File was expected to be an image but was found to be ${file.type}`);
        } else {
          const filedContentType: string = field + 'ContentType';
          this.dataUtils.toBase64(file, base64Data => {
            this.editForm.patchValue({
              [field]: base64Data,
              [filedContentType]: file.type
            });
          });
        }
      } else {
        reject(`Base64 data was not set as file could not be extracted from passed parameter: ${event}`);
      }
    }).then(
      // eslint-disable-next-line no-console
      () => console.log('blob added'), // success
      this.onError
    );
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const certificate = this.createFromForm();
    if (certificate.id !== undefined) {
      this.subscribeToSaveResponse(this.certificateService.update(certificate));
    } else {
      this.subscribeToSaveResponse(this.certificateService.create(certificate));
    }
  }

  private createFromForm(): ICertificate {
    return {
      ...new Certificate(),
      id: this.editForm.get(['id']).value,
      tbsDigest: this.editForm.get(['tbsDigest']).value,
      subject: this.editForm.get(['subject']).value,
      issuer: this.editForm.get(['issuer']).value,
      type: this.editForm.get(['type']).value,
      description: this.editForm.get(['description']).value,
      subjectKeyIdentifier: this.editForm.get(['subjectKeyIdentifier']).value,
      authorityKeyIdentifier: this.editForm.get(['authorityKeyIdentifier']).value,
      fingerprint: this.editForm.get(['fingerprint']).value,
      serial: this.editForm.get(['serial']).value,
      validFrom: this.editForm.get(['validFrom']).value,
      validTo: this.editForm.get(['validTo']).value,
      creationExecutionId: this.editForm.get(['creationExecutionId']).value,
      contentAddedAt: this.editForm.get(['contentAddedAt']).value,
      revokedSince: this.editForm.get(['revokedSince']).value,
      revocationReason: this.editForm.get(['revocationReason']).value,
      revoked: this.editForm.get(['revoked']).value,
      revocationExecutionId: this.editForm.get(['revocationExecutionId']).value,
      content: this.editForm.get(['content']).value,
      csr: this.editForm.get(['csr']).value,
      issuingCertificate: this.editForm.get(['issuingCertificate']).value
    };
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICertificate>>) {
    result.subscribe(() => this.onSaveSuccess(), () => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackCSRById(index: number, item: ICSR) {
    return item.id;
  }

  trackCertificateById(index: number, item: ICertificate) {
    return item.id;
  }
}
