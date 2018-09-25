var app = new Vue ({
  el: '#main',
  data: {
    searchString: '',
    previewURL: 'https://www.neimanmarcus.com/',
    // hardcoded for simplicity
    // should use ajax
    articles: [
      {
        'theme':'Theme 1',
        'url':'#'
      }
    ]
  },
  methods:{
    loadPreview: function(url) {
      this.previewURL = url;
    },
    fetchThematicPages: function(searchQuery) {
      axios
      .post("URL")
      .then(response => (this.articles = response['response']['docs']))
      .catch(err => console.log(err))
    }

  },
  computed: {
    }
});