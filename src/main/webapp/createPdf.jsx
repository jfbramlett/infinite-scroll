import React from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import CircularProgress from 'material-ui/CircularProgress';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import DatePicker from 'material-ui/DatePicker';

class CreatePdf extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            exporting: 0,
            template: 1
        };
      }

      handleExportClick = (event) => {
          // This prevents ghost click.
          event.preventDefault();

          this.setState({
            exporting: 1
          });
        };

        handleGeneratePdfClick = () => {
          this.setState({
            exporting: 2
          });

          var blogId = this.props.blogId;
          var startDate = this.state.startDate.toISOString().substring(0, 10);
          var endDate = this.state.endDate.toISOString().substring(0, 10);
          var source = new EventSource("/blog-export/generate/" + blogId + "?fromDate=" + startDate + "&toDate=" + endDate);
          source.onmessage = (e) => {
            if (JSON.parse(e.data).status.startsWith("Completed")) {
              this.setState({exporting: 0});
              source.close();
              this.props.refreshBlogs();
            }
          };
          source.onerror = (e) => {
            console.log("Failed creating pdf: " + e.data);
            this.setState({exporting: 0});
            source.close();
          };

        };

        handleGeneratePdfCancelClick = () => {
          this.setState({
            exporting: 0,
          });
        };

        handleTemplateChange = (event, index, value) => this.setState({template : value});

        handleStartDateChange = (event, newDate) => {
          this.setState({
            startDate: newDate,
          });
        };

        handleEndDateChange = (event, newDate) => {
          this.setState({
            endDate: newDate,
          });
        };

      render() {
        var exporting = this.state.exporting;
        let display = null;

        if (exporting === 0) {
            display = (
                <RaisedButton
                  onClick={this.handleExportClick}
                  label="Export as PDF"
                />
            );
        } else {
            if (exporting === 1) {
                display = (
                    <div>
                        <DatePicker hintText="Start Date" autoOk={true} onChange={this.handleStartDateChange}/>
                        <DatePicker hintText="End Date" autoOk={true} onChange={this.handleEndDateChange}  />
                        <SelectField floatingLabelText="Template"
                            value={this.state.template}
                            onChange={this.handleTemplateChange}>
                          <MenuItem value={1} primaryText="Left Wrap Images" />
                        </SelectField>
                        <br />
                        <RaisedButton
                          onClick={this.handleGeneratePdfClick}
                          label="Export"
                        />
                        <RaisedButton
                          onClick={this.handleGeneratePdfCancelClick}
                          label="Cancel"
                        />
                    </div>
                );
            } else {
                display = (
                    <div>
                        <CircularProgress />
                    </div>
                );
            }
        }

		return (
			<div>
                {display}
            </div>
		)
	}
};

export default CreatePdf;
