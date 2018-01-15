'use strict';

// tag::vars[]
const React = require('react');
const ReactDOM = require('react-dom');
const client = require('./client');
// end::vars[]

// tag::app[]
class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {educations: []};
    }

    componentDidMount() {
        client({method: 'GET', path: '/api/educations'}).done(response => {
            this.setState({educations: response.entity.educations});
        });
    }

    render() {
        return (
            <ul>
                <li>Educations</li>
                <ElementList key="0" elements={this.state.educations}/>
            </ul>
        )
    }
}

class ElementList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {elements: []}
    }

    componentDidMount() {
        if (this.props.ids != null && this.props.ids.length > 0) {
            fetch('/api/all_ids', {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(this.props.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({elements: responseJson});
            });
        }
    }

    componentDidUpdate() {
        if(this.props.elements != null && this.state.elements.length == 0) {
            console.log("elements updated");
            this.setState({elements: this.props.elements});
        }
    }

    render() {
        var elements = this.state.elements.map(elem =>
            <Element key={elem.id} elem={elem}/>);
        return (
            <li>
                {elements}
            </li>
        )
    }
}

class Element extends React.Component {

    render() {
        console.log(this.props.elem.id);
        var elem = this.props.elem;
        switch(elem.type) {
            case 'Education':
                return this.renderEducation(elem);
            case 'DegreeProgramme':
                //return this.renderDegreeProgramme(elem);
            case 'GroupingModule':
                //return this.renderGroupingModule(elem);
            case 'StudyModule':
                //return this.renderStudyModule(elem);
                return this.renderOtherTypes(elem);
            default:
                return (
                    <ul>
                        <li>
                            tuntematon tyyppi {elem.type}
                        </li>
                    </ul>
                )
        }
    }

    renderEducation(elem) {

        var structure = getElementStructure(elem.structure);
        console.log("stucture");
        console.log(structure);

        return(
            <ul>
                <li><b>{elem.name.fi}</b> (Education)</li>
                <li>
                    Tutkinnon rakenne<br/>
                    {structure}
                </li>
            </ul>
        )
    }

    renderOtherTypes(elem) {
        var rules = parseRule(elem.rule);
        return (
            <ul>
                <li><b>{elem.name.fi}</b></li>
                <li>{elem.type}</li>
                {elem.targetCredits != null && <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>}
                {rules.modules.length > 0 &&
                <li>
                    Osat:<br/>
                    <ul>
                        <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
                    </ul>
                </li>}
                <li>
                    Kurssit: <br/>
                    <CourseList key={'cu-' + elem.id} ids={rules.courses}/>
                </li>
            </ul>
        )
    }

    //renderDegreeProgramme(elem) {
    //    var rules = parseRule(elem.rule);
    //    console.log("groupingmodule: " + elem.id);
    //    console.log(rules);
    //    // parse elements from list
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
    //            <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>
    //        </ul>
    //    )
    //}
    //
    //renderGroupingModule(elem) {
    //    var rules = parseRule(elem.rule);
    //    console.log("groupingmodule: " + elem.id)
    //    console.log(rules);
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>
    //        </ul>
    //    )
    //}
    //
    //renderStudyModule(elem) {
    //    var rules = parseRule(elem.rule);
    //    return (
    //        <ul>
    //            <li><b>{elem.name.fi}</b></li>
    //            <li>{elem.type}</li>
    //            <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
    //            {rules.modules.length > 0 &&
    //                <li>
    //                Osat:<br/>
    //                <ul>
    //                    <ElementList key={'mods-' + elem.id} ids={rules.modules}/>
    //                </ul>
    //            </li>}
    //            <li>
    //                Kurssit: <br/>
    //                {rules.courses}
    //            </li>
    //        </ul>
    //    )
    //}

}

function parseRule(rule) {
    var modules = [];
    var courses = [];
    var response;
    if(rule.rule != null) {
        response = parseRule(rule.rule);
        modules = modules.concat(response.modules);
        courses = courses.concat(response.courses);
    } else if(rule.rules != null) {
        for(var i = 0; i < rule.rules.length; i++) {
            response = parseRule(rule.rules[i]);
            modules = modules.concat(response.modules);
            courses = courses.concat(response.courses);
        }

    } else {
        if(rule.type == 'ModuleRule') {
            modules.push(rule.moduleGroupId);
        } else if(rule.type == 'CourseUnitRule') {
            courses.push(rule.courseUnitGroupId);
        }
    }
    return { modules: modules, courses: courses }
}

function getRules(rule) {
    var rules = [];
    if(rule.type == 'CompositeRule') {
        var subModIds = [];
        for(var i = 0; i < rule.rules.length; i++) {
            var sub = rule.rules[i];
            if(sub.type == 'CompositeRule') {
                rules.concat(getRules(sub));
            }
            if(sub.type == 'ModuleRule') {
                subModIds.push(sub.moduleGroupId);
            }
        }
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} ids={subModIds}/></li>);
    } else if(rule.type == 'ModuleRule') {
        var mods = [];
        mods.push(rule.moduleGroupId);
        rules.push(<li key={'l-' + rule.localId}><ElementList key={rule.localId} ids={mods}/></li>);
    }
    return rules;
}

function getElementStructure(struct) {
    var structures = [];
    for(var property in struct) {
        if(property.startsWith("phase") && struct[property] != null)  {
            var phase = struct[property];
            var options = [];
            for(var i = 0; i < phase.options.length; i++) {
                options.push(phase.options[i].moduleGroupId);
            }
            structures.push(<ul key={property}>
                <li>{phase.name.fi}</li>
                <li><ElementList key={'opt-' + property} ids={options}/></li>
            </ul>)
        }
    }
    return <ul>{structures}</ul>;

}

class CourseList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {courseNames: []}
    }

    componentDidMount() {
        if (this.props.ids != null && this.props.ids.length > 0) {
            fetch('/api/cu/names', {
                method: 'post',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(this.props.ids)
            }).then((response) => response.json()).then(responseJson => {
                this.setState({courseNames: responseJson});
            });
        }
    }


    render() {
        var courseNames = this.state.courseNames.map((name, index) => <li key={index + "" + name.fi}>{name.fi}</li>);
        return (
            <ul>
                {courseNames}
            </ul>
        )
    }


}

// tag::render[]
ReactDOM.render(
    <App />,
    document.getElementById('react')
)
// end::render[]
