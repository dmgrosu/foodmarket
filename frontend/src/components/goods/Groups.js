import React from 'react';
import {TreeItem, TreeView} from "@material-ui/lab";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
    root: {
        height: 600,
        flexGrow: 1,
        overflow: 'auto',
        padding: theme.spacing(2)
    },
    formControl: {
        margin: theme.spacing(2),
    },
});

const Groups = ({classes, expanded, selected, handleToggle, handleSelect, groups}) => {

    const renderTree = (group) => (
        <TreeItem key={group.id} nodeId={group.id.toString()} label={group.name}>
            {Array.isArray(group.groups) ? group.groups.map(group => renderTree(group)) : null}
        </TreeItem>
    );

    return (
        <TreeView
            className={classes.root}
            defaultCollapseIcon={<ExpandMoreIcon/>}
            defaultExpandIcon={<ChevronRightIcon/>}
            expanded={expanded}
            selected={selected}
            onNodeToggle={handleToggle}
            onNodeSelect={handleSelect}
        >
            {Array.isArray(groups) ?
                groups.map(group => renderTree(group)) :
                <TreeItem nodeId={"0"} label={"no groups found"}/>}
        </TreeView>
    )
};

export default withStyles(styles)(Groups);
