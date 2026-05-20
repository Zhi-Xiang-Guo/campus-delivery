var baseUrl = 'http://localhost:80';
var uploadedImagePrefix = 'http://localhost:8080/images/';

function normalizeOrderId(value) {
  if (value == null) return '';
  if (typeof value === 'number' || typeof value === 'string') {
    var id = String(value).trim();
    return /^\d+$/.test(id) ? id : '';
  }
  if (value.id != null) return String(value.id);
  if (value.orderId != null) return String(value.orderId);
  if (value.currentTarget && value.currentTarget.dataset) {
    var dataset = value.currentTarget.dataset;
    if (dataset.id != null) return String(dataset.id);
    if (dataset.orderId != null) return String(dataset.orderId);
    if (dataset.orderid != null) return String(dataset.orderid);
  }
  return '';
}

function normalizeTempImagePath(path) {
  if (!path) return path;
  return path;
}

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
          wx.showToast({ title: res.data.msg || '操作失败', icon: 'none' });
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
    orderId: '',
    orderDetailList: [],
    score: 5,
    scoreText: '非常满意',
    content: '',
    images: [],
    submitting: false
  },

  methods: {
    onLoad: function(options) {
      var that = this;
      var orderId = normalizeOrderId(options && options.orderId);
      if (orderId) {
        that.setData({ orderId: orderId });
        that.fetchOrderDetail(orderId);
      } else {
        wx.showToast({ title: '订单信息异常，请返回后重试', icon: 'none' });
      }
    },

    fetchOrderDetail: function(orderId) {
      var that = this;
      request('/user/order/orderDetail/' + orderId, 'GET').then(function(res) {
        if (res.data && res.data.orderDetailList) {
          that.setData({ orderDetailList: res.data.orderDetailList });
        }
      });
    },

    chooseScore: function(e) {
      var score = e.currentTarget.dataset.score;
      var scoreTextMap = {
        1: '非常不满意',
        2: '不太满意',
        3: '一般',
        4: '满意',
        5: '非常满意'
      };
      this.setData({
        score: score,
        scoreText: scoreTextMap[score] || '非常满意'
      });
    },

    onContentInput: function(e) {
      this.setData({ content: e.detail.value });
    },

    goBack: function() {
      wx.navigateBack();
    },

    chooseImage: function() {
      var that = this;
      wx.chooseImage({
        count: 6 - that.data.images.length,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: function(res) {
          var tempFilePaths = res.tempFilePaths || [];
          var newImages = that.data.images.concat(tempFilePaths.map(normalizeTempImagePath));
          that.setData({ images: newImages });
        }
      });
    },

    deleteImage: function(e) {
      var index = e.currentTarget.dataset.index;
      var images = this.data.images;
      images.splice(index, 1);
      this.setData({ images: images });
    },

    uploadSingleImage: function(filePath) {
      var token = wx.getStorageSync('auth_token') || '';
      if (!token) {
        return Promise.reject({
          msg: '登录已失效，请重新进入小程序后再试',
          statusCode: 401
        });
      }
      return new Promise(function(resolve, reject) {
        wx.uploadFile({
          url: baseUrl + '/user/common/upload',
          filePath: normalizeTempImagePath(filePath),
          name: 'file',
          timeout: 30000,
          header: { 'authentication': token },
          success: function(res) {
            if (res.statusCode === 401) {
              reject({
                msg: '登录已失效，请重新进入小程序后再试',
                statusCode: 401
              });
              return;
            }

            var data = res.data;
            if (typeof data === 'string') {
              if (!data) {
                reject({
                  msg: '图片上传失败，请重新登录后重试',
                  statusCode: res.statusCode
                });
                return;
              }
              try {
                data = JSON.parse(data);
              } catch (e) {
                console.error('图片上传接口返回异常', {
                  statusCode: res.statusCode,
                  data: data
                });
                reject({
                  msg: '图片上传接口返回异常',
                  detail: data ? data.substring(0, 120) : '',
                  statusCode: res.statusCode
                });
                return;
              }
            }
            if (res.statusCode !== 200) {
              reject(data || { msg: '图片上传失败', statusCode: res.statusCode });
              return;
            }
            if (data.code === 1 || data.code === 200) {
              resolve(data.data);
            } else {
              reject(data);
            }
          },
          fail: function(err) {
            reject({
              msg: err && err.errMsg ? err.errMsg : '图片上传失败，请重新选择图片后重试',
              detail: err
            });
          }
        });
      });
    },

    uploadImages: function() {
      var that = this;
      if (that.data.images.length === 0) return Promise.resolve([]);

      var uploadPromises = that.data.images.map(function(path) {
        if (that.isUploadedImage(path)) {
          return Promise.resolve(path);
        }
        return that.uploadSingleImage(path);
      });
      return Promise.all(uploadPromises);
    },

    submitComment: function() {
      var that = this;
      if (that.data.submitting) return;

      that.setData({ submitting: true });

      that.uploadImages().then(function(imageUrls) {
        var params = {
          orderId: Number(normalizeOrderId(that.data.orderId)),
          score: that.data.score,
          content: that.data.content,
          images: imageUrls
        };

        request('/user/comment/submit', 'POST', params).then(function(res) {
          wx.showToast({ title: '评价成功', icon: 'success' });
          setTimeout(function() {
            wx.navigateBack();
          }, 1500);
        }).catch(function() {
          that.setData({ submitting: false });
        });
      }).catch(function(err) {
        that.setData({ submitting: false });
        var message = '图片上传失败，请重新登录或压缩图片后重试';
        if (err && err.statusCode === 401) {
          message = '登录已失效，请重新进入小程序后再试';
        } else if (err && err.msg) {
          message = err.msg;
        }
        wx.showToast({ title: message, icon: 'none' });
      });
    },

    isUploadedImage: function(path) {
      if (!path) return false;
      return path.indexOf(uploadedImagePrefix) === 0
        || path.indexOf('/images/') === 0
        || path.indexOf('images/') === 0;
    }
  }
});
