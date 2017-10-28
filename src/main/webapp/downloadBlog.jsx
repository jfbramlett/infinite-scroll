import React from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import CircularProgress from 'material-ui/CircularProgress';

class DownloadBlog extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            downloading : false
        }
      }

      handleDownloadBlogClick = () => {
        this.setState({ downloading : true});

        var blogId = this.props.blogId;
        var source = new EventSource("/blog-export/download/" + blogId);
        source.onmessage = (e) => {
          if (JSON.parse(e.data).status.startsWith("Completed")) {
            this.setState({downloading: false});
            source.close();
            this.props.refreshBlogs();
          }
        };
        source.onerror = (e) => {
          console.log("Failed downloading blog: " + e.data);
          this.setState({downloading: false});
          source.close();
        };
      }

      render() {
          const downloadingBlog = this.state.downloading;

          let display = null;

          if (downloadingBlog) {
              display = (
                  <div>
                      <CircularProgress />
                  </div>
                  );
          } else {
              display = <RaisedButton label="Download" primary={true} onClick={this.handleDownloadBlogClick} />
          }

		return (
			<div>
                {display}
			</div>
		)
	}
};


export default DownloadBlog;
