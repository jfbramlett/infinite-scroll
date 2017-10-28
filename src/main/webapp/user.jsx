import React from 'react';
import 'whatwg-fetch'

class User extends React.Component {
	constructor(props) {
		super(props);
		this.state = {firstName : "", lastName : "", email : ""};
	}

	loadUser = () => {
		fetch('/blog-export/user', {
			method: 'GET',
            credentials: 'same-origin',
			headers: {
				'Content-Type' : 'application/json'
			},
			mode: 'no-cors'
		}).then(response => {
			return response.json()
		}).then(json => {
			this.setState({firstName: json["firstName"], lastName : json["lastName"], email : json["email"]})
		}).catch(function(ex) {
			console.log('failed to process blogs', ex)
		})
	}

	componentDidMount() {
		this.loadUser();
	}

	render() {
		return (
			<div style={{textAlign: 'right' }}>
				Welcome {this.state.firstName} &nbsp;
				<a href="/blog-export/logout">Logout</a>
			</div>
		)
	}
};


export default User;
