function ArticleCtrl($scope) {
  $scope.articles = [];

  $scope.getMoreArticles = function() {
    var url = 'board/article/list'
    if ($scope.articles != []) {
      // TODO
    }
    $.get(url, function(data) {
      console.log(data)

      //  likeStyle: "",
      //  unlikeStyle: "display: none",
      //  getCommentsStyle: "",
    })
  }

  $scope.like = function(article) {
    article.likeStyle = "display: none"
    article.unlikeStyle = ""
    $.post('board/article/like/' + article.id, function() {
      // TODO
    })
  };

  $scope.unlike = function(article) {
    article.unlikeStyle = "display: none"
    article.likeStyle = ""
    $.post('board/article/unlike/' + article.id, function() {
      // TODO
    })
  };

  $scope.getComments = function(article) {
    article.getCommentsStyle = "display: none"
    $.post('board/comment/list/' + article.id, function(data) {
      console.log(data)
      // TODO
    })
  };

  $scope.getMoreArticles();
}
