import React from 'react';
import Blogs from './blogs.jsx';
import injectTapEventPlugin from "react-tap-event-plugin"
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';

injectTapEventPlugin();

export default class App extends React.Component {
  render() {
    return (
      <MuiThemeProvider>
				<Blogs />
      </MuiThemeProvider>
    )
  }
}
