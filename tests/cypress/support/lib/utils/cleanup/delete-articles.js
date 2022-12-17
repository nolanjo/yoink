/// <reference types="cypress" />

import { vivoApiCallVerified, vivoApiCallChain } from "../api/api-calls";

export function deleteArticles() {
    getAllPublishedArticles().then(res => {
        // delete the articles made by automation if there are more than 4
        if (res.body.items.length > 4) {
            for (const article of res.body.items) {
                if (article.title.match(/\d{13}/)) {
                    deleteArticle(article.id);
                }
            }
        }
    });
}

function getAllPublishedArticles() {
    return vivoApiCallVerified("api/manage/articles?term=&type=published&page=1&limit=20");
}

function deleteArticle(articleId) {
    vivoApiCallChain(`api/articles/${articleId}/archive`, "POST");
    vivoApiCallChain(`api/articles/${articleId}`, "DELETE");
}
