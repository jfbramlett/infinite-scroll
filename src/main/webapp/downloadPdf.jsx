import React from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import Popover from 'material-ui/Popover';
import Menu from 'material-ui/Menu';
import MenuItem from 'material-ui/MenuItem';
import 'whatwg-fetch'

class DownloadPdf extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            open: false,
            availableDownloads : []
        };
      }

      handleClick = (event) => {
          // This prevents ghost click.
          event.preventDefault();

          this.setState({
            open: true,
            anchorEl: event.currentTarget,
          });
        };

        handleRequestClose = () => {
          this.setState({
            open: false,
          });
        };

      componentDidMount() {
        var blogId = this.props.blogId;

        fetch('/blog-export/listdownloads/' + blogId, {
          method: 'GET',
          credentials: 'same-origin',
          headers: {
            'Content-Type' : 'application/json'
          },
          mode: 'no-cors'
        }).then(response => {
          return response.json()
        }).then(json => {
          this.setState({availableDownloads: json})
        }).catch(function(ex) {
          console.log('failed to process blogs', ex)
        })
      }


      render() {
		return (
			<div>
                <RaisedButton
                      onClick={this.handleClick}
                      label={this.props.blogName}
                    />
                <Popover open={this.state.open}
                      anchorEl={this.state.anchorEl}
                      anchorOrigin={{horizontal: 'left', vertical: 'bottom'}}
                      targetOrigin={{horizontal: 'left', vertical: 'top'}}
                      onRequestClose={this.handleRequestClose}
                    >
                      <Menu>
                          {this.state.availableDownloads.map(function(listValue){
                              return <MenuItem key={listValue} primaryText={listValue} />
                          })}
                    </Menu>
                </Popover>
            </div>
		)
	}
};


export default DownloadPdf;
