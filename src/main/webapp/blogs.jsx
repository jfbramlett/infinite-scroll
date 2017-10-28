import React from 'react';
import {Table, TableHeader, TableRow, TableHeaderColumn, TableBody, TableRowColumn} from 'material-ui/Table';
import AddBlog from './addBlog';
import DownloadBlog from './downloadBlog';
import DownloadPdf from './downloadPdf';
import CreatePdf from './createPdf';
import User from './user';
import 'whatwg-fetch'

class Blogs extends React.Component {
	constructor(props) {
		super(props);
		this.state = {addingBlog : false,
					blogs: []};
	}

	loadBlogs = () => {
		fetch('/blog-export/blogs', {
			method: 'GET',
            credentials: 'same-origin',
			headers: {
				'Content-Type' : 'application/json'
			},
			mode: 'no-cors'
		}).then(response => {
			return response.json()
		}).then(json => {
			this.setState({blogs: json})
		}).catch(function(ex) {
			console.log('failed to process blogs', ex)
		})
	}

	componentDidMount() {
		this.loadBlogs();
	}

	render() {
		return (
			<div>
				<User />
				<h1>Blogs</h1>
				<Table>
					<TableHeader displaySelectAll={false} adjustForCheckbox={false} enableSelectAll={false}>
						<TableRow>
							<TableHeaderColumn>Name</TableHeaderColumn>
							<TableHeaderColumn>Last Download</TableHeaderColumn>
							<TableHeaderColumn></TableHeaderColumn>
							<TableHeaderColumn></TableHeaderColumn>
						</TableRow>
					</TableHeader>
					<TableBody stripedRows={true} displayRowCheckbox={false}>
							{this.state.blogs.map((listValue) => {
								return <TableRow key={listValue.name}>
											<TableRowColumn><DownloadPdf blogName={listValue.name} blogId={listValue.id}/></TableRowColumn>
											<TableRowColumn>{listValue.lastDownload}</TableRowColumn>
											<TableRowColumn><DownloadBlog blogId={listValue.id} refreshBlogs={this.loadBlogs}/></TableRowColumn>
											<TableRowColumn><CreatePdf blogId={listValue.id} refreshBlogs={this.loadBlogs}/></TableRowColumn>
										</TableRow>;
							})}
					</TableBody>
				</Table>
				<AddBlog refreshBlogs={this.loadBlogs}/>
			</div>
		)
	}
};


export default Blogs;
