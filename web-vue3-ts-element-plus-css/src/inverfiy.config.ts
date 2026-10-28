import { Container } from 'inversify';
import {
    ID_LOGIN_SERVICE, ID_STORE_SERVICE, ID_REQUEST_SERVICE, ID_COMM_SERVICE, ID_MESSAGE_SERVICE,
    ID_APP_PRESENTER,
    ID_BASE_SERVICE,
    ID_MAIN_PAGE_PRESENTER,
    ID_INFO_SERVICE, ID_SYSTEM_SERVICE, ID_PERSON_SERVICE, ID_TEACHING_SERVICE,
    ID_DICTIONARY_MANAGE_PRESENTER, ID_MENU_MANAGE_PRESENTER,
    ID_PASSWORD_PRESENTER, ID_SYSTEM_INTRODUCE_PRESENTER, ID_STUDENT_INTRODUCE_PRESENTER,
    ID_STUDENT_PRESENTER, ID_FAMILY_MEMBER_PRESENTER,
    ID_COURSE_PRESENTER, ID_SCORE_PRESENTER,
} from './types';
import { ILoginService } from '~/infrastructure/boundaries/login-service';
import { LoginServiceImpl } from '~/infrastructure/services/login-service-impl';
import { IStoreService } from '~/infrastructure/boundaries/store-service';
import { StoreServiceImpl } from '~/infrastructure/services/store-service-impl';
import { IRequestService } from '~/infrastructure/boundaries/request-service';
import { RequestServiceImpl } from '~/infrastructure/services/request-service-impl';
import { ICommService } from '~/infrastructure/boundaries/comm-service';
import { CommServiceImpl } from '~/infrastructure/services/comm-service-impl';
import { IMessageService } from '~/infrastructure/boundaries/message-service';
import { MessageServiceImpl } from '~/infrastructure/services/message-service-impl';
import { AppPresenter } from '~/infrastructure/presenters/app-presenter';

import { IBaseService } from '~/infrastructure/boundaries/base-service';
import { BaseServiceImpl } from '~/infrastructure/services/base-service-impl';

import { MainPagePresenter } from '~/infrastructure/presenters/main-page-presenter';

import { IInfoService } from '~/domain/boundaries/info-service';
import { InfoServiceImpl } from '~/domain/services/info-service-impl';
import { ISystemService } from '~/domain/boundaries/system-service';
import { SystemServiceImpl } from '~/domain/services/system-service-impl';
import { IPersonService } from '~/domain/boundaries/person-service';
import { PersonServiceImpl } from '~/domain/services/person-service-impl';
import { ITeachingService } from '~/domain/boundaries/teaching-service';
import { TeachingServiceImpl } from '~/domain/services/teaching-service-impl';

import { DictionaryManagePresenter } from '~/domain/presenters/dictionary-manage-presenter';
import { MenuManagePresenter } from '~/domain/presenters/menu-manage-presenter';

import { PasswordPresenter } from '~/domain/presenters/password-presenter';
import { StudentIntroducePresenter } from '~/domain/presenters/student-introduce-presenter';
import { SystemIntroducePresenter } from '~/domain/presenters/system-introduce-presenter';

import { StudentPresenter } from '~/domain/presenters/student-presenter';
import { FamilyMemberPresenter } from '~/domain/presenters/family-member-presenter';

import { CoursePresenter } from '~/domain/presenters/course-presenter';
import { ScorePresenter } from '~/domain/presenters/score-presenter';


const container = new Container();
container.bind<ILoginService>(ID_LOGIN_SERVICE).to(LoginServiceImpl).inSingletonScope();
container.bind<IStoreService>(ID_STORE_SERVICE).to(StoreServiceImpl).inSingletonScope();
container.bind<IRequestService>(ID_REQUEST_SERVICE).to(RequestServiceImpl).inSingletonScope();
container.bind<ICommService>(ID_COMM_SERVICE).to(CommServiceImpl).inSingletonScope();
container.bind<IMessageService>(ID_MESSAGE_SERVICE).to(MessageServiceImpl).inSingletonScope();
container.bind<AppPresenter>(ID_APP_PRESENTER).to(AppPresenter).inSingletonScope();

container.bind<IBaseService>(ID_BASE_SERVICE).to(BaseServiceImpl).inSingletonScope();

container.bind<MainPagePresenter>(ID_MAIN_PAGE_PRESENTER).to(MainPagePresenter).inSingletonScope();

container.bind<IInfoService>(ID_INFO_SERVICE).to(InfoServiceImpl).inSingletonScope();
container.bind<ISystemService>(ID_SYSTEM_SERVICE).to(SystemServiceImpl).inSingletonScope();
container.bind<IPersonService>(ID_PERSON_SERVICE).to(PersonServiceImpl).inSingletonScope();
container.bind<ITeachingService>(ID_TEACHING_SERVICE).to(TeachingServiceImpl).inSingletonScope();

container.bind<DictionaryManagePresenter>(ID_DICTIONARY_MANAGE_PRESENTER).to(DictionaryManagePresenter).inSingletonScope();
container.bind<MenuManagePresenter>(ID_MENU_MANAGE_PRESENTER).to(MenuManagePresenter).inSingletonScope();

container.bind<PasswordPresenter>(ID_PASSWORD_PRESENTER).to(PasswordPresenter).inSingletonScope();
container.bind<StudentIntroducePresenter>(ID_STUDENT_INTRODUCE_PRESENTER).to(StudentIntroducePresenter).inSingletonScope();
container.bind<SystemIntroducePresenter>(ID_SYSTEM_INTRODUCE_PRESENTER).to(SystemIntroducePresenter).inSingletonScope();

container.bind<StudentPresenter>(ID_STUDENT_PRESENTER).to(StudentPresenter).inSingletonScope();
container.bind<FamilyMemberPresenter>(ID_FAMILY_MEMBER_PRESENTER).to(FamilyMemberPresenter).inSingletonScope();

container.bind<CoursePresenter>(ID_COURSE_PRESENTER).to(CoursePresenter).inSingletonScope();
container.bind<ScorePresenter>(ID_SCORE_PRESENTER).to(ScorePresenter).inSingletonScope();

export { container };