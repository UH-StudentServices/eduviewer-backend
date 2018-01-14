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
                return this.renderDegreeProgramme(elem);
            case 'GroupingModule':
                return this.renderGroupingModule(elem);
            case 'StudyModule':
                return this.renderStudyModule(elem);
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

    renderDegreeProgramme(elem) {
        var rules = getRules(elem.rule);
        return (
            <ul>
                <li><b>{elem.name.fi}</b></li>
                <li>{elem.type}</li>
                <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
                <li>
                    Osat:<br/>
                    <ul>
                        {rules}
                    </ul>
                </li>
            </ul>
        )
    }

    renderGroupingModule(elem) {
        var rules = getRules(elem.rule);
        return (
            <ul>
                <li><b>{elem.name.fi}</b></li>
                <li>{elem.type}</li>
                <li>
                    Osat:<br/>
                    <ul>
                        {rules}
                    </ul>
                </li>
            </ul>
        )
    }

    renderStudyModule(elem) {
        var rules = getRules(elem.rule);
        return (
            <ul>
                <li><b>{elem.name.fi}</b></li>
                <li>{elem.type}</li>
                <li>Opintoviikot {elem.targetCredits.min} - {elem.targetCredits.max}</li>
                <li>
                    Osat:<br/>
                    <ul>
                        {rules}
                    </ul>
                </li>
            </ul>
        )
    }

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

// end::app[]

// tag::employee-list[]
class EducationList extends React.Component{
    render() {
        var educations = this.props.educations.map(education =>
            <Education key={education.id} education={education}/>
        );
        console.log("EDucations: ");
        console.log(educations);
        return (
            <ul>
                {educations}
            </ul>
        )
    }
}
// end::employee-list[]

// tag::employee[]
class Education extends React.Component{
    render() {
        console.log(this);
        var structureElements = getStructureElements(this.props.education.structure);
        return (
            <li>
                <ul>
                    <li>{this.props.education.name.fi}</li>
                    <li>{this.props.education.type}</li>
                    <li>{this.props.education.id}</li>
                    <li>
                        <StructureList structures={this.props.education.structure}/>
                    </li>
                </ul>
            </li>
        )
    }
}

class StructureList extends React.Component {
    render() {
        console.log("structure: ");
        console.log(this);
        var phases = getStructureElements(this.props.structures)
        console.log(phases);
        var structures = phases.map(ent => <Structure key={ent.key} elem={ent.data}/>);
        return(<ul>
            {structures}
        </ul>)
    }
}

class Structure extends React.Component {
    render() {
        var options = getModuleGroups(this.props.elem.options);
        return(<ul>
            <li>{this.props.elem.name.fi}</li>
            {options}
        </ul>)
    }
}

class ModuleGroupList extends React.Component {

    constructor(props) {
        super(props);
        this.state = {modules: []};
    }

    componentDidMount() {
        console.log("Mounting module");
        console.log(this);
        client({method: 'GET', path: '/api/by_group_id/' + this.props.groupId}).done(response => {
            this.setState({modules: response.entity});
        });

    }

    render() {

        var modules = this.state.modules.map(module =>
            <Module key={module.id} module={module}/>
        );
        return (
            <ul>
                <li>mg</li>
                {modules}
            </ul>
        )
    }

}

class Module extends React.Component {

    render() {
        var subModules = getStructureElements(this.props.module.structure);
        return (
            <ul>
                <li>{this.props.module.name.fi}</li>
                <li>
                    {subModules}
                </li>
            </ul>
        )
    }
}

function getStructureElements(elem) {
    var phases = [];
    for(var property in elem) {
        if(property.startsWith("phase") && elem[property] != null)  {
            phases.push({ key: property, data: elem[property]});
        }
    }
    return phases;

}


function getModuleGroups(node) {
    var elements = [];
    if(node == null) {
        return elements;
    }
    for(var i = 0; i < node.length; i++) {
        var option = node[i];
        elements.push(<ModuleGroupList key={option.localId} groupId={option.moduleGroupId}/>);
    }
    return elements;
}

function getRuleElements(node) {
    var subElements = [];
    if(node == null) {
        return subElements;
    }

    if(node.type == 'ModuleRule') {
        subElements.push(getSubElement(node));
        return subElements;
    }
    if(node.type == 'CompositeRule') {
        for(var i = 0; i < node.length; i++) {
            var element = getSubElement(node[i]);
            if(element != null) {
                subElements.push(element);
            }
        }
    }
    return subElements;
}

function getSubElement(element) {
    if(element == null) {
        return null;
    } else if(element.type == 'ModuleRule') {
        return <ModuleGroupList groupId={element.moduleGroupId}/>
    }
}

// tag::render[]
ReactDOM.render(
    <App />,
    document.getElementById('react')
)
// end::render[]
