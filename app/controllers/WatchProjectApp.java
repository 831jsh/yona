/**
 * Yona, 21st Century Project Hosting SW
 * <p>
 * Copyright Yona & Yobi Authors & NAVER Corp.
 * https://yona.io
 **/
package controllers;

import controllers.annotation.AnonymousCheck;
import controllers.annotation.IsAllowed;
import models.Project;
import models.User;
import models.UserProjectNotification;
import models.Watch;
import models.enumeration.EventType;
import models.enumeration.Operation;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.AccessControl;
import utils.ErrorViews;

@AnonymousCheck(requiresLogin = true, displaysFlashMessage = true)
public class WatchProjectApp extends Controller {

    @IsAllowed(Operation.READ)
    @Transactional
    public static Result watch(String userName, String projectName) {
        Project project = Project.findByOwnerAndProjectName(userName, projectName);
        Watch.watch(project.asResource());
        return ok();
    }

    @IsAllowed(Operation.READ)
    @Transactional
    public static Result unwatch(String userName, String projectName) {
        Project project = Project.findByOwnerAndProjectName(userName, projectName);
        Watch.unwatch(project.asResource());
        return ok();
    }

    public static Result toggle(Long projectId, String notificationType) {
        EventType notiType = EventType.valueOf(notificationType);
        Project project = Project.find.byId(projectId);
        User user = UserApp.currentUser();

        if(project == null) {
            return notFound(ErrorViews.NotFound.render("error.notfound.project"));
        }
        if(!AccessControl.isAllowed(user, project.asResource(), Operation.READ)) {
            return forbidden(ErrorViews.Forbidden.render("error.forbidden", project));
        }
        if(!Watch.isWatching(user, project.asResource())) {
            return badRequest(Messages.get("error.notfound.watch"));
        }

        UserProjectNotification upn = UserProjectNotification.findOne(user, project, notiType);
        if(upn == null) { // make the EventType OFF, because default is ON.
            UserProjectNotification.unwatchExplictly(user, project, notiType);
        } else {
            upn.toggle();
        }

        return ok();
    }
}
