package com.github.checkit.util;

import com.github.checkit.model.Change;
import com.github.checkit.model.Comment;
import com.github.checkit.model.Notification;
import com.github.checkit.model.PublicationContext;
import com.github.checkit.model.User;
import com.github.checkit.model.Vocabulary;
import cz.cvut.kbss.jopa.model.MultilingualString;

public final class NotificationTemplateUtil {

    /**
     * Creates template for created publication context.
     *
     * @param pc publication context
     * @return Notification template
     */
    public static Notification getForCreatedPublicationContext(PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "New publication context").set("cs", "Nový publikační kontext"));
        notification.setContent(new MultilingualString().set("en",
            String.format("Publication context \"%s\" with vocabulary you gestor was created.",
                pc.getFromProject().getLabel())).set("cs",
            String.format("Byl vytvořen publikační kontext \"%s\" obsahující vámi gestorovaný slovník.",
                pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getPublicationDetailPath(pc.getId()));
        return notification;
    }

    /**
     * Creates template for created publication context with message for admin about newly created vocabulary.
     *
     * @param pc publication context
     * @return Notification template
     */
    public static Notification getForCreatedPublicationContextForAdmin(PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "New vocabulary").set("cs", "Nový slovník"));
        notification.setContent(new MultilingualString().set("en",
            String.format("Publication context \"%s\" with newly created vocabulary was submitted.",
                pc.getFromProject().getLabel())).set("cs",
            String.format("Byl vytvořen publikační kontext \"%s\" s novým slovníkem.",
                pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getPublicationDetailPath(pc.getId()));
        return notification;
    }

    /**
     * Creates template for updated publication context.
     *
     * @param pc publication context
     * @return Notification template
     */
    public static Notification getForUpdatedPublicationContext(PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "Update of publication context").set("cs", "Aktualizace publikačního "
                + "kontextu"));
        notification.setContent(new MultilingualString().set("en",
            String.format("Publication context \"%s\" with your review was updated.",
                pc.getFromProject().getLabel())).set("cs",
            String.format("Byl aktualizován publikační kontext \"%s\", který jste revidovali.",
                pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getPublicationDetailPath(pc.getId()));
        return notification;
    }

    /**
     * Creates template for discussion comment on change.
     *
     * @param comment created comment
     * @param change  change which was commented
     * @param pc      publication context of change
     * @return Notification template
     */
    public static Notification getForDiscussionComment(Comment comment, Change change, PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "New comment").set("cs", "Nový komentář"));
        notification.setContent(new MultilingualString().set("en",
                String.format("%s commented on change about \"%s\" in publication context \"%s\".",
                    comment.getAuthor().getFullName(), Utils.resolveMultilingual(change.getLabel(), "en"),
                    pc.getFromProject().getLabel()))
            .set("cs",
                String.format("%s okomentoval změnu ohledně \"%s\" v publikačním kontextu \"%s\".",
                    comment.getAuthor().getFullName(), Utils.resolveMultilingual(change.getLabel(), "cs"),
                    pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getChangeInVocabularyInPublicationContextPath(pc.getId(),
            change.getContext().getBasedOnVersion().toString(), change.getId()));
        return notification;
    }

    /**
     * Creates template for rejection comment on change.
     *
     * @param comment created comment
     * @param change  change which was commented
     * @param pc      publication context of change
     * @return Notification template
     */
    public static Notification getForRejectionCommentOnChange(Comment comment, Change change, PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "Change rejected").set("cs", "Změna zamítnuta"));
        notification.setContent(new MultilingualString().set("en",
                String.format("%s rejected change about \"%s\" in publication context \"%s\", where you contributed.",
                    comment.getAuthor().getFullName(), Utils.resolveMultilingual(change.getLabel(), "en"),
                    pc.getFromProject().getLabel()))
            .set("cs",
                String.format("%s zamítnul změnu ohledně \"%s\" v publikačním kontextu \"%s\", na kterém jste se "
                        + "podílel/a.",
                    comment.getAuthor().getFullName(), Utils.resolveMultilingual(change.getLabel(), "cs"),
                    pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getChangeInVocabularyInPublicationContextPath(pc.getId(),
            change.getContext().getBasedOnVersion().toString(), change.getId()));
        return notification;
    }

    /**
     * Creates template for approved publication context.
     *
     * @param comment created comment
     * @param pc      publication context of comment
     * @return Notification template
     */
    public static Notification getForApprovingCommentOnPublicationContext(Comment comment,
                                                                          PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "Publication context approved").set("cs", "Publikační kontext "
                + "schválen"));
        notification.setContent(new MultilingualString().set("en",
                String.format("%s approved publication context \"%s\", where you contributed.",
                    comment.getAuthor().getFullName(), pc.getFromProject().getLabel()))
            .set("cs",
                String.format("%s schválil publikační kontext \"%s\", na kterém jste se podílel/a.",
                    comment.getAuthor().getFullName(), pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getPublicationDetailPath(pc.getId()));
        return notification;
    }

    /**
     * Creates template for rejected publication context.
     *
     * @param comment created comment
     * @param pc      publication context of comment
     * @return Notification template
     */
    public static Notification getForRejectionCommentOnPublicationContext(Comment comment,
                                                                          PublicationContext pc) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "Publication context rejected").set("cs", "Publikační kontext "
                + "zamítnut"));
        notification.setContent(new MultilingualString().set("en",
                String.format("%s rejected publication context \"%s\", where you contributed.",
                    comment.getAuthor().getFullName(), pc.getFromProject().getLabel()))
            .set("cs",
                String.format("%s zamítnul publikační kontext \"%s\", na kterém jste se podílel/a.",
                    comment.getAuthor().getFullName(), pc.getFromProject().getLabel())));
        notification.setAbout(FrontendPaths.getPublicationDetailPath(pc.getId()));
        return notification;
    }

    /**
     * Creates template for created gestoring request.
     *
     * @param applicant  user applying to gestor
     * @param vocabulary vocabulary
     * @return Notification template
     */
    public static Notification getForCreatedGestoringRequest(User applicant, Vocabulary vocabulary) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "New gestoring request").set("cs", "Nový požadavek na gestorování"));
        notification.setContent(new MultilingualString().set("en",
                String.format("%s requests to gestor vocabulary \"%s\".",
                    applicant.getFullName(), vocabulary.getLabel()))
            .set("cs",
                String.format("%s žádá o gestorování slovníku \"%s\".",
                    applicant.getFullName(), vocabulary.getLabel())));
        notification.setAbout(FrontendPaths.getGestoringRequests());
        return notification;
    }

    /**
     * Creates template for resolved gestoring request.
     *
     * @param vocabulary vocabulary
     * @param approved   if gestoring request was approved or not
     * @return Notification template
     */
    public static Notification getForResolvedGestoringRequest(Vocabulary vocabulary, boolean approved) {
        Notification notification = new Notification();
        notification.setTitle(
            new MultilingualString().set("en", "Resolved gestoring request").set("cs", "Vyřízený požadavek na "
                + "gestorování"));
        notification.setContent(new MultilingualString().set("en",
                String.format("Your gestoring request for vocabulary \"%s\" was %s",
                    vocabulary.getLabel(), approved ? "approved. You can now review changes made to this vocabulary." :
                                           "rejected."))
            .set("cs",
                String.format("Vaše žádost o gestorování slovníku \"%s\" byla %s",
                    vocabulary.getLabel(), approved ? "schválena. Nyní můžete revidovat změny provedené pro tento "
                        + "slovník." : "zamítnuta.")));
        notification.setAbout(null);
        return notification;
    }
}
