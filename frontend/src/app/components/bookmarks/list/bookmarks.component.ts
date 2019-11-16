import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { MatTableDataSource, MatPaginator, MatSort } from '@angular/material';
import { Bookmark } from '../bookmarks.model';
import { BookmarksService } from '../bookmarks.service';
import { JmsResource } from 'src/app/api/jms-session';

@Component({
  selector: 'app-bookmarks',
  templateUrl: './bookmarks.component.html',
  styleUrls: ['./bookmarks.component.scss']
})
// tslint:disable: curly
export class BookmarksComponent implements OnInit {

  @ViewChild(MatPaginator, {static: false}) paginator: MatPaginator;
  @ViewChild(MatSort, {static: false}) sort: MatSort;

  @Input() connectorId: number;
  dataSource = new MatTableDataSource<Bookmark>([]);
  newBookmark: Bookmark = {};

  constructor(private bookmarksService: BookmarksService) { }

  ngOnInit() {
    this.doLoad();
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
    this.bookmarksService.list(this.connectorId, null).subscribe(result => {
      this.dataSource.disconnect();
      this.dataSource = new MatTableDataSource(result.content);
      this.dataSource.paginator = this.paginator;
      this.dataSource.sort = this.sort;
      if (this.paginator) this.paginator.length = result.numberOfElements;
    });
  }

  doDelete(bookmark: Bookmark) {
    this.bookmarksService.delete(this.connectorId, bookmark.id).subscribe(r => this.doLoad());
  }

  doBookmark(resource: JmsResource) {
    this.bookmarksService.save(this.connectorId, {name: resource.name, type: resource.type})
                         .subscribe(r => this.doLoad());
  }

  doAdd() {
    this.bookmarksService.save(this.connectorId, this.newBookmark)
      .subscribe(r => {
        this.newBookmark = {};
        this.doLoad();
      });
  }
}