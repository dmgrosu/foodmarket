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

const Groups = ({
                    classes, expanded, selected, handleToggle, handleSelect, groups,
                    childrenByGroupId, loadingGroupIds, isFetching
                }) => {

    const {t} = useTranslation();

    // One level is fetched at a time, so a node that says it has children may not have them yet.
    // MUI only draws the expander when a TreeItem has a child, so an unloaded branch gets a
    // placeholder to expand onto.
    const renderTree = (group) => {
        const nodeId = group.id.toString();
        const loaded = childrenByGroupId[nodeId];
        return (
            <TreeItem key={nodeId} nodeId={nodeId} label={group.name}>
                {!group.hasChildren ? null
                    : loaded ? loaded.map(child => renderTree(child))
                        : <TreeItem nodeId={`${nodeId}-loading`}
                                    label={loadingGroupIds.includes(nodeId)
                                        ? t('products.groups.loading')
                                        : ''}/>}
            </TreeItem>
        );
    };

    const selectGroup = (e, nodeId) => {
        if (nodeId !== '0' && !nodeId.endsWith('-loading')) {
            handleSelect(e, nodeId);
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
            {Array.isArray(groups) && groups.length > 0 ?
                groups.map(group => renderTree(group)) :
                <TreeItem nodeId={"0"} label={t('products.groups.noGroupsFound')}/>}
        </TreeView>
    )
};

export default withStyles(styles)(Groups);
