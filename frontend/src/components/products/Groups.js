import React from 'react';
import {TreeItem, TreeView} from "@material-ui/lab";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import ChevronRightIcon from '@material-ui/icons/ChevronRight';
import {CircularProgress, withStyles} from "@material-ui/core";
import {useTranslation} from "react-i18next";

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
    progress: {
        margin: 'auto',
        display: 'flex',
        position: 'relative',
        top: '40%',
    }
});

const Groups = ({classes, expanded, selected, handleToggle, handleSelect, groups, isFetching}) => {

    const {t} = useTranslation();

    const renderTree = (group) => (
        <TreeItem key={group.id} nodeId={group.id.toString()} label={group.name}>
            {Array.isArray(group.children) ? group.children.map(group => renderTree(group)) : null}
        </TreeItem>
    );

    const selectGroup = (e, groupId) => {
        if (groupId !== '0') {
            handleSelect(e, groupId);
        }
    }

    if (isFetching) {
        return (
            <div className={classes.root}>
                <CircularProgress className={classes.progress} size={60}/>
            </div>
        );
    }

    return (
        <TreeView
            className={classes.root}
            defaultCollapseIcon={<ExpandMoreIcon/>}
            defaultExpandIcon={<ChevronRightIcon/>}
            expanded={expanded}
            selected={selected}
            onNodeToggle={handleToggle}
            onNodeSelect={selectGroup}
        >
            {Array.isArray(groups) ?
                groups.map(group => renderTree(group)) :
                <TreeItem nodeId={"0"} label={t('products.groups.noGroupsFound')}/>}
        </TreeView>
    )
};

export default withStyles(styles)(Groups);
