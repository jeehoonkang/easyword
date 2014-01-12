function ArticleCtrl($scope) {
  $scope.articles = []

  function preprocess(article) {
    if (article.comments == undefined) article.comments = []
    if (article.newComment == undefined) article.newComment = ""
    if (article.getCommentsStyle == undefined) article.getCommentsStyle = article.numComments == 0 ? "display: none" : ""
    if (article.updateStyle == undefined) article.updateStyle = (article.authorEmail == $('#userId').val()) ? "" : "display: none"

    if (article.likeStyle == undefined) article.likeStyle = article.liked ? "display: none" : ""
    if (article.unlikeStyle == undefined) article.unlikeStyle = article.liked ? "" : "display: none"
  }

  $scope.getMoreArticles = function() {
    var url = 'board/article/list'
    if ($scope.articles != []) {
      // TODO
    }

    $.get(url, function(articles) {
      $.each(articles, function(index, article) {
        preprocess(article)

        article.index = $scope.articles.length
        $scope.articles.push(article)
        if (!$scope.$$phase) $scope.$apply()
      })
    })
  }

  $scope.like = function(article) {
    article.likeStyle = "display: none"
    article.unlikeStyle = ""
    $.post('board/article/like/' + article.id, function(newArticle) {
      newArticle.comments = article.comments
      newArticle.newComment = article.newComment
      newArticle.getCommentsStyle = article.getCommentsStyle
      preprocess(newArticle)

      var index = article.index
      var newComment = article.newComment
      newArticle.index = index
      newArticle.newComment = newComment
      $scope.articles[index] = newArticle
      if (!$scope.$$phase) $scope.$apply()
    })
  }

  $scope.unlike = function(article) {
    article.unlikeStyle = "display: none"
    article.likeStyle = ""
    $.post('board/article/unlike/' + article.id, function(newArticle) {
      newArticle.comments = article.comments
      newArticle.newComment = article.newComment
      newArticle.getCommentsStyle = article.getCommentsStyle
      preprocess(newArticle)

      var index = article.index
      var newComment = article.newComment
      newArticle.index = index
      newArticle.newComment = newComment
      $scope.articles[index] = newArticle
      if (!$scope.$$phase) $scope.$apply()
    })
  }

  function renderComments(article, comments) {
    article.getCommentsStyle = "display: none"
    article.comments = comments
    if (!$scope.$$phase) $scope.$apply()
  }

  $scope.getComments = function(article) {
    article.getCommentsStyle = "display: none"
    $.get('board/comment/list/' + article.id, function(comments) {
      renderComments(article, comments)
    })
  }

  $scope.createComment = function(event, article) {      
    if (!event.shiftKey && event.which == 13) {
      $.post('board/comment/create/' + article.id,
             {text: article.newComment},
             function(comments) {
               renderComments(article, comments)

               $(event.target).removeAttr('disabled')
               article.newComment = ""
               if (!$scope.$$phase) $scope.$apply()
             })
      $(event.target).attr('disabled', 'disabled')
    }
  }

  $scope.getMoreArticles()
}
