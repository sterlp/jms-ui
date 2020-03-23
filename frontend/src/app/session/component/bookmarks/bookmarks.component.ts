import { Component, OnInit, Input, ViewChild, OnDestroy, AfterViewInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { JmsResource } from 'src/app/api/jms-session';
import { Bookmark } from '../../service/bookmarks/bookmarks.model';
import { BookmarksService } from '../../service/bookmarks/bookmarks.service';
import { Observable } from 'rxjs';
import { Pageable } from '@sterlp/ng-spring-boot-api';
import { LoadingHelper } from 'src/app/common/loading/loading.helper';
import { MatGridTileHeaderCssMatStyler } from '@angular/material/grid-list';
import { ErrorDialogService } from 'src/app/common/error-dialog/error-dialog.service';

@Component({
  selector: 'app-bookmarks',
  templateUrl: './bookmarks.component.html',
  styleUrls: ['./bookmarks.component.scss']
})
// tslint:disable: curly
export class BookmarksComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild(MatPaginator) paginator: MatPaginator;
    @ViewChild(MatSort) sort: MatSort;
    @Input() connectorId: number;

    dataSource = new MatTableDataSource<Bookmark>([]);
    pageable = new Pageable();
    newBookmark: Bookmark = {};

    private lh = new LoadingHelper();
    loading$ = this.lh.loading$;

    constructor(private bookmarksService: BookmarksService, private errorDialog: ErrorDialogService) { }

    ngOnInit() {}
    ngAfterViewInit(): void {
        this.pageable.size = 2000; // no paging yet
        setTimeout(() => this.doLoad());
    }
    ngOnDestroy(): void {
        this.lh.close();
    }

    applyFilter(filterValue: string) {
        filterValue = filterValue.trim(); // Remove whitespace
        filterValue = filterValue.toLowerCase(); // Datasource defaults to lowercase matches
        this.dataSource.filter = filterValue;
        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage();
        }
    }

    doLoad() {
        this.pageable.addFilter('connectorId', this.connectorId);
        this.lh.loading();
        this.bookmarksService.list(this.pageable).subscribe(result => {
            this.dataSource.disconnect();
            this.dataSource = new MatTableDataSource(result.content);
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
            if (this.paginator) this.paginator.length = result.numberOfElements;
        },
        this.errorDialog.showError('Failed to load the bookmarks.'),
        () => this.lh.done());
    }

    doDelete(bookmark: Bookmark) {
        this.bookmarksService.delete(bookmark.id).subscribe(r => this.doLoad());
    }

    doBookmark(resource: JmsResource) {
        this.bookmarksService.save({name: resource.name, type: resource.type}, this.connectorId)
                             .subscribe(r => this.doLoad());
    }

    doAdd() {
        this.bookmarksService.save(this.newBookmark, this.connectorId)
            .subscribe(r => {
                this.dataSource.data.splice(0, 0, r);
                this.dataSource.data = this.dataSource.data;
                this.newBookmark = {};
            });
    }
}
