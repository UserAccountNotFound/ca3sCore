import { IAuthorization } from '@/shared/model/authorization.model';

export const enum ChallengeStatus {
  PENDING = 'PENDING',
  VALID = 'VALID',
  INVALID = 'INVALID',
  DEACTIVATED = 'DEACTIVATED',
  EXPIRED = 'EXPIRED',
  REVOKED = 'REVOKED'
}

export interface IAcmeChallenge {
  id?: number;
  challengeId?: number;
  type?: string;
  value?: string;
  token?: string;
  validated?: Date;
  status?: ChallengeStatus;
  authorization?: IAuthorization;
}

export class AcmeChallenge implements IAcmeChallenge {
  constructor(
    public id?: number,
    public challengeId?: number,
    public type?: string,
    public value?: string,
    public token?: string,
    public validated?: Date,
    public status?: ChallengeStatus,
    public authorization?: IAuthorization
  ) {}
}
