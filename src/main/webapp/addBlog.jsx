import React from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';
import 'whatwg-fetch'

class AddBlog extends React.Component {
    constructor(props) {
        super(props);
        this.state = {addingBlog: false};
      }

      handleAddBlogSubmit = () => {
        var blogUrl = this.state.blogUrl;
        fetch("/blog-export/addBlog?url=" + blogUrl, {
    			method: 'GET',
                credentials: 'same-origin',
    			headers: {
    				'Content-Type' : 'application/json'
    			},
    			mode: 'no-cors'
    		}).then(response => {
    			this.props.refreshBlogs();
    		}).catch(function(ex) {
    			console.log('failed to process blogs', ex)
    		})

        this.setState({addingBlog: false, blogUrl: ''});
      }

      handleAddBlogCancelClick = () => {
        this.setState({addingBlog: false});
      }

      handleAddBlogClick = () => {
        this.setState({addingBlog: true});
      }

      handleBlogUrlEntry = (event, value) => {
        this.setState({blogUrl : value});
      }

      render() {
        const addingBlog = this.state.addingBlog;

        let display = null;

        if (addingBlog) {
            display = (
                <div>
                    <TextField floatingLabelText="Blog URL" hintText="Blog URL: https://life-in-newyork.blogspot.com/" onChange={this.handleBlogUrlEntry} /><br />
                    <RaisedButton label="Add" primary={true} onClick={this.handleAddBlogSubmit} />
                    <RaisedButton label="Cancel"  onClick={this.handleAddBlogCancelClick}/>
                </div>
                );
        } else {
            display = <RaisedButton label="Add Blog" primary={true} onClick={this.handleAddBlogClick} />
        }

		return (
			<div>
                {display}
			</div>
		)
	}
};


export default AddBlog;
