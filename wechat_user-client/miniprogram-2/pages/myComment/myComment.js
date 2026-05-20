var baseUrl = 'http://localhost:80';

function request(url, method, params) {
  var token = wx.getStorageSync('auth_token') || '';
  return new Promise(function(resolve, reject) {
    wx.request({
      url: baseUrl + url,
      data: params,
      header: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
        'authentication': token
      },
      method: method,
      success: function(res) {
        if (res.data.code === 1 || res.data.code === 200) {
          resolve(res.data);
        } else {
          wx.showToast({ title: res.data.msg || '加载失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail: function(err) {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
        reject(err);
      }
    });
  });
}

Component({
  data: {
    commentList: [],
    page: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: true
  },

  methods: {
    onLoad: function() {
      this.loadComments();
    },

    onPullDownRefresh: function() {
      this.setData({ page: 1, commentList: [], hasMore: true });
      this.loadComments();
      wx.stopPullDownRefresh();
    },

    onReachBottom: function() {
      if (this.data.commentList.length < this.data.total) {
        this.setData({ page: this.data.page + 1 });
        this.loadComments();
      }
    },

    loadComments: function() {
      var that = this;
      if (that.data.loading || !that.data.hasMore) return;
      that.setData({ loading: true });

      request('/user/comment/page', 'GET', {
        page: that.data.page,
        pageSize: that.data.pageSize
      }).then(function(res) {
        var records = (res.data.records || []).map(function(item) {
          var formatted = Object.assign({}, item);
          formatted.createTime = that.formatTime(item.createTime);
          formatted.images = item.images || [];
          formatted.orderItems = item.orderItems || [];
          return formatted;
        });
        var list = that.data.commentList.concat(records);
        that.setData({
          commentList: list,
          total: res.data.total || 0,
          loading: false,
          hasMore: list.length < (res.data.total || 0)
        });
      }).catch(function() {
        that.setData({ loading: false });
      });
    },

    formatTime: function(dateStr) {
      if (!dateStr) return '';
      return dateStr.replace('T', ' ').substring(0, 19);
    },

    goBack: function() {
      wx.navigateBack();
    }
  }
});
